package saulo.brustolin.project.dtos.transactions;

import saulo.brustolin.project.entities.TransactionType;

import java.time.Instant;

import saulo.brustolin.project.entities.CollectionType;
import saulo.brustolin.project.entities.Transaction;

public record TransactionEvent(
    String transactionId,
    String description,
    Integer amount,
    TransactionType type,
    CollectionType collection,
    Instant date,
    String email,
    String fullname
) {
    public final static TransactionEvent fromEntity(Transaction transaction, String email, String fullname) {
        return new TransactionEvent(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCollection(),
            transaction.getDate(),
            email,
            fullname
        );
    }
}
