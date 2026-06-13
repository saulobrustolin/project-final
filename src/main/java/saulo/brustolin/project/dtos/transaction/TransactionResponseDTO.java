package saulo.brustolin.project.dtos.transaction;

import saulo.brustolin.project.entities.TransactionType;
import saulo.brustolin.project.entities.CollectionType;

public record TransactionResponseDTO(
    String transactionId,
    String description,
    Integer amount,
    TransactionType type,
    CollectionType collection
) {}
