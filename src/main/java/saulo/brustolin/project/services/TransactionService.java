package saulo.brustolin.project.services;

import java.time.Instant;
import java.time.LocalTime;
import java.time.YearMonth;
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
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.mappers.TransactionMapper;
import saulo.brustolin.project.repositories.TransactionRepository;

@Service
@AllArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public void createTransaction(User user, CreateTransactionDTO dto) {
        Transaction transaction = new Transaction(dto.description(), dto.amount(), user.getId(), dto.type(), dto.collection(), dto.date());

        transactionRepository.save(transaction);
    }

    public TransactionResponseDTO getTransaction(User user, String transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));
            
        return new TransactionResponseDTO(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCollection()
        );
    }

    @Transactional
    public void updateTransaction(User user, String transactionId, UpdateTransactionDTO dto) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        transactionMapper.updateEntityFromDto(dto, transaction);
    }

    public void deleteTransaction(User user, String transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        transactionRepository.delete(transaction);
    }

    public List<TransactionResponseDTO> getPeriod(User user, YearMonth period) {
        YearMonth target = period != null ? period : YearMonth.now();

        Instant start = target.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = target.atEndOfMonth().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

        List<Transaction> transactions = transactionRepository.findAllByUserIdAndDateBetween(user.getId(), start, end);

        return transactions.stream().map(TransactionResponseDTO::fromEntity).toList();
    }
}