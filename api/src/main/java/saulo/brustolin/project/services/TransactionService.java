package saulo.brustolin.project.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.configurations.RabbitMQConfig;
import saulo.brustolin.project.dtos.transactions.CreateTransactionDTO;
import saulo.brustolin.project.dtos.transactions.DeletionType;
import saulo.brustolin.shared.dtos.*;
import saulo.brustolin.shared.entities.*;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.transactions.UpdateTransactionDTO;
import saulo.brustolin.project.entities.Transaction;
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

    public static final String EXCHANGE_NAME = "notifications.v1.events";

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#user.id"),
            @CacheEvict(value = "transactions", key = "#user.id")
    })
    public void createTransaction(User user, CreateTransactionDTO dto) {
        boolean isSingle = dto.recurrence() == null && dto.subdivision() == null;
        String groupId = isSingle ? null : UUID.randomUUID().toString();
        for (Integer i = 1; i <= dto.recurrence(); i++) {
            ZonedDateTime zoned = dto.date().atZone(ZoneId.of("UTC"));

            Instant date = zoned.plusMonths(i - 1).toInstant();


            Transaction transaction = new Transaction(dto.description(), dto.amount(), user.getId(), dto.type(),
                    dto.collection(), date, groupId);

            try {
                user.setBalance(
                        user.getBalance() + (transaction.getType() == TransactionType.INCOME ? transaction.getAmount()
                                : -transaction.getAmount()));
                userRepository.save(user);

                if (dto.subdivision() != null && Optional.of(dto.subdivision()).isPresent()) {
                    createSubdivisionTransactions(user, transaction, dto.subdivision());
                    return;
                }

                transactionRepository.save(transaction);
            } finally {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        "transaction.created",
                        new TransactionEvent(transaction.getId(), transaction.getDescription(), transaction.getAmount(),
                                transaction.getType(), transaction.getCollection(), transaction.getDate(),
                                user.getEmail(),
                                user.getName()));
            }
        }
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
                transaction.getDate());
    }

    @Transactional
    @Caching(put = {
            @CachePut(value = "transactions", key = "#transactionId")
    }, evict = {
            @CacheEvict(value = "users", key = "#user.id")
    })
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
        userRepository.save(user);

        if (dto.subdivision() != null && Optional.of(dto.subdivision()).isPresent()) {
            createSubdivisionTransactions(user, transaction, dto.subdivision());
            return;
        };

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                "transaction.updated",
                new TransactionEvent(transactionId, transaction.getDescription(), transaction.getAmount(),
                        transaction.getType(), transaction.getCollection(), transaction.getDate(), user.getEmail(),
                        user.getName()));

        transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(User user, String transactionId, DeletionType type) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        if (!transaction.getUserId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.UNAUTHORIZED, "Essa transação é privada");
        }

        if (type == DeletionType.ALL) {
            transactionRepository.deleteAllByGroupId(transaction.getGroupId());
        } else if (type == DeletionType.NEXT) {
            transactionRepository.deleteAllByGroupIdAndDateGreaterThan(transaction.getGroupId(), transaction.getDate());
        } else {
            transactionRepository.delete(transaction);
        }

        user.setBalance(user.getBalance() - (transaction.getType() == TransactionType.INCOME ? transaction.getAmount()
                : -transaction.getAmount()));
        userRepository.save(user);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                "transaction.deleted",
                new TransactionEvent(transactionId, transaction.getDescription(), transaction.getAmount(),
                        transaction.getType(), transaction.getCollection(), transaction.getDate(), user.getEmail(),
                        user.getName()));
    }

    public List<TransactionResponseDTO> getPeriod(User user, Integer month, Integer year) {
        ZoneId zoneId = ZoneId.systemDefault();
        YearMonth yearMonth = YearMonth.of(year, month);

        Instant start = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant();
        Instant end = LocalDateTime.of(yearMonth.atEndOfMonth(), LocalTime.MAX).atZone(zoneId).toInstant();

        List<Transaction> transactions = transactionRepository.findAllByUserIdAndDateBetween(user.getId(), start, end);

        return transactions.stream().map(TransactionResponseDTO::fromEntity).toList();
    }

    public List<TransactionResponseDTO> allTransactionsAt(User user, Integer month, Integer year) {
        ZoneId zoneId = ZoneId.systemDefault();
        YearMonth yearMonth = YearMonth.of(year, month);

        Instant end = LocalDateTime.of(yearMonth.atEndOfMonth(), LocalTime.MAX).atZone(zoneId).toInstant();

        List<Transaction> transactions = transactionRepository.findAllByUserIdAndDateLessThan(user.getId(), end);

        return transactions.stream().map(TransactionResponseDTO::fromEntity).toList();
    }

    public void createSubdivisionTransactions(User user, Transaction transaction, Integer subdivision) {
        List<Transaction> transactions = new ArrayList<Transaction>();
        Integer subdivisionValue = transaction.getAmount() / subdivision;

        String groupId = UUID.randomUUID().toString();
        for (Integer i = 1; i <= subdivision; i++) {
            ZonedDateTime baseDate = transaction.getDate().atZone(ZoneId.of("UTC"));
            Transaction t = new Transaction(
                    String.format("%d de %d | %s", i, subdivision, transaction.getDescription()),
                    subdivisionValue,
                    user.getId(),
                    transaction.getType(),
                    transaction.getCollection(),
                    baseDate.plusMonths(i - 1).toInstant(),
                    groupId);

            transactions.add(t);
        }

        if (transaction.getId() != null && Optional.of(transaction.getId()).isPresent())
            deleteTransaction(user, transaction.getId(), null);

        transactionRepository.saveAll(transactions);
    }

    public Integer calculateBalance(List<TransactionResponseDTO> transactions) {
        Integer credit = transactions.stream()
                .filter(t -> t.type() == TransactionType.EXPENSE)
                .mapToInt(t -> t.amount())
                .sum();

        Integer debit = transactions.stream()
                .filter(t -> t.type() == TransactionType.INCOME)
                .mapToInt(t -> t.amount())
                .sum();

        return debit - credit;
    }
}