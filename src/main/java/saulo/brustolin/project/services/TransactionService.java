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
import saulo.brustolin.project.dtos.transactions.CreateTransactionDTO;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.transactions.UpdateTransactionDTO;
import saulo.brustolin.project.entities.Transaction;
import saulo.brustolin.project.entities.TransactionType;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.mappers.TransactionMapper;
import saulo.brustolin.project.repositories.TransactionRepository;
import saulo.brustolin.project.repositories.UserRepository;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;

    @Transactional
    public void createTransaction(User user, CreateTransactionDTO dto) {
        Transaction transaction = new Transaction(dto.description(), dto.amount(), user.getId(), dto.type(),
                dto.collection(), dto.date());

        transactionRepository.save(transaction);

        user.setBalance(user.getBalance() + (transaction.getType() == TransactionType.INCOME ? transaction.getAmount()
                : -transaction.getAmount()));

        userRepository.save(user);
    }

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
    public void updateTransaction(User user, String transactionId, UpdateTransactionDTO dto) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        System.out.println(user.getBalance());
        if (dto.amount() != null) {
            if (transaction.getType() == TransactionType.INCOME) {
                user.setBalance(user.getBalance() - transaction.getAmount());
            } else {
                user.setBalance(user.getBalance() + transaction.getAmount());
            }
            System.out.println(user.getBalance());

            if (transaction.getType() == TransactionType.INCOME) {
                user.setBalance(user.getBalance() + dto.amount());
            } else {
                user.setBalance(user.getBalance() - dto.amount());
            }
            System.out.println(user.getBalance());
        }

        transactionMapper.updateEntityFromDto(dto, transaction);

        transactionRepository.save(transaction);
        userRepository.save(user);
    }

    @Transactional
    public void deleteTransaction(User user, String transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        if (transaction.getUserId() != user.getId()) {
            throw new ErrorException(HttpStatus.UNAUTHORIZED, "Essa transação é privada");
        }

        transactionRepository.delete(transaction);
        user.setBalance(user.getBalance() - (transaction.getType() == TransactionType.INCOME ? transaction.getAmount()
                : -transaction.getAmount()));
        userRepository.save(user);
    }

    public List<TransactionResponseDTO> getPeriod(User user, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "É obrigatório tem data inicial");
        }
        if (to == null) {
            to = from;
        }

        Instant start = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = from.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

        List<Transaction> transactions = transactionRepository.findAllByUserIdAndDateBetween(user.getId(), start, end);

        return transactions.stream().map(TransactionResponseDTO::fromEntity).toList();
    }
}