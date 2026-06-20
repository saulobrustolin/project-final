package saulo.brustolin.project.dtos.transactions;

import saulo.brustolin.shared.entities.TransactionType;

import java.time.Instant;

import saulo.brustolin.shared.entities.CollectionType;
import saulo.brustolin.project.entities.Transaction;

public record TransactionResponseDTO(
    String transactionId,
    String description,
    Integer amount,
    TransactionType type,
    CollectionType collection,
    Instant date
) {
    public final static TransactionResponseDTO fromEntity(Transaction transaction) {
        return new TransactionResponseDTO(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCollection(),
            transaction.getDate()
        );
    }
}
