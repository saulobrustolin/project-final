package saulo.brustolin.project.dtos.transaction;

import saulo.brustolin.project.entities.TransactionType;
import saulo.brustolin.project.entities.CollectionType;
import saulo.brustolin.project.entities.Transaction;

public record TransactionResponseDTO(
    String transactionId,
    String description,
    Integer amount,
    TransactionType type,
    CollectionType collection
) {
    public final static TransactionResponseDTO fromEntity(Transaction transaction) {
        return new TransactionResponseDTO(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCollection()
        );
    }
}
