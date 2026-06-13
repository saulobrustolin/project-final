package saulo.brustolin.project.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.transaction.CreateTransactionDTO;
import saulo.brustolin.project.dtos.transaction.TransactionResponseDTO;
import saulo.brustolin.project.entities.Transaction;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.repositories.TransactionRepository;

@Service
@AllArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;

    public void createTransaction(User user, CreateTransactionDTO dto) {
        Transaction transaction = new Transaction(dto.description(), dto.amount(), user.getId(), dto.type(), dto.collection());

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
}