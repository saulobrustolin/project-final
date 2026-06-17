package saulo.brustolin.shared.dtos;

import java.time.Instant;

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