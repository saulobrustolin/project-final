package saulo.brustolin.notification.models;

import java.time.Instant;

public record TransactionEvent(
    String transactionId,
    String description,
    Integer amount,
    TransactionType type,
    CollectionType collection,
    Instant date,
    String email,
    String fullname
) {}