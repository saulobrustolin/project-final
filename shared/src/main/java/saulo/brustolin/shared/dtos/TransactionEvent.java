package saulo.brustolin.shared.dtos;

import java.time.Instant;

import saulo.brustolin.shared.entities.CollectionType;
import saulo.brustolin.shared.entities.TransactionType;

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