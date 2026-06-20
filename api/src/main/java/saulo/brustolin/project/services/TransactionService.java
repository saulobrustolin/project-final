package saulo.brustolin.project.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.configurations.RabbitMQConfig;
import saulo.brustolin.project.dtos.transactions.CreateTransactionDTO;
import saulo.brustolin.project.dtos.transactions.TransactionEvent;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.transactions.UpdateTransactionDTO;
import saulo.brustolin.project.entities.Transaction;
import saulo.brustolin.project.entities.TransactionType;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.mappers.TransactionMapper;
import saulo.brustolin.project.repositories.TransactionRepository;
import saulo.brustolin.project.repositories.UserRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    public static final String QUEUE_NAME = "notifications.v1.transaction-created";
    public static final String EXCHANGE_NAME = "notifications.v1.events";
    public static final String ROUTING_KEY = "transaction.created";

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#user.id"),
        @CacheEvict(value = "transactions", key = "#user.id")
    })
    public void createTransaction(User user, CreateTransactionDTO dto) {
        Transaction transaction = new Transaction(dto.description(), dto.amount(), user.getId(), dto.type(),
                dto.collection(), dto.date());

        transactionRepository.save(transaction);

        user.setBalance(user.getBalance() + (transaction.getType() == TransactionType.INCOME ? transaction.getAmount()
                : -transaction.getAmount()));

        userRepository.save(user);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            "transaction-created",
            TransactionEvent.fromEntity(transaction, user.getEmail(), user.getName())
        );
    }

    @Cacheable(value = "transactions", key = "#transactionId")
    public TransactionResponseDTO getTransaction(User user, String transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        return new TransactionResponseDTO(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCollection(),
            transaction.getDate()
        );
    }

    @Transactional
    @Caching(
        put = {
            @CachePut(value = "transactions", key = "#transactionId")
        },
        evict = {
            @CacheEvict(value = "users", key = "#user.id")
        }
    )
    public void updateTransaction(User user, String transactionId, UpdateTransactionDTO dto) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        if (dto.amount() != null && !dto.amount().equals(transaction.getAmount())) {
            Integer currentBalance = user.getBalance();
            Integer oldAmount = transaction.getAmount();
            Integer newAmount = dto.amount();

            if (transaction.getType() == TransactionType.INCOME) {
                user.setBalance(currentBalance - oldAmount + newAmount);
            } else {
                user.setBalance(currentBalance + oldAmount - newAmount);
            }
        }

        transactionMapper.updateEntityFromDto(dto, transaction);

        transactionRepository.save(transaction);
        userRepository.save(user);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            "transaction.updated",
            TransactionEvent.fromEntity(transaction, user.getEmail(), user.getName())
        );
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#user.id"),
        @CacheEvict(value = "transactions", key = "#transactionId")
    })
    public void deleteTransaction(User user, String transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        if (!transaction.getUserId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.UNAUTHORIZED, "Essa transação é privada");
        }

        transactionRepository.delete(transaction);
        user.setBalance(user.getBalance() - (transaction.getType() == TransactionType.INCOME ? transaction.getAmount()
                : -transaction.getAmount()));
        userRepository.save(user);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            "transaction-deleted",
            TransactionEvent.fromEntity(transaction, user.getEmail(), user.getName())
        );
    }

    public List<TransactionResponseDTO> getPeriod(User user, LocalDate from, LocalDate to) {
        if (to == null) {
            to = from;
        }

        Instant start = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = to.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

        List<Transaction> transactions = transactionRepository.findAllByUserIdAndDateBetween(user.getId(), start, end);

        return transactions.stream().map(TransactionResponseDTO::fromEntity).toList();
    }
}