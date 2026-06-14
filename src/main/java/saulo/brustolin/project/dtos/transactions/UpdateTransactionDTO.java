package saulo.brustolin.project.dtos.transactions;

import jakarta.validation.constraints.Positive;
import saulo.brustolin.project.entities.CollectionType;
import saulo.brustolin.project.entities.TransactionType;

public record UpdateTransactionDTO(
    String description,
    @Positive Integer amount,
    TransactionType type,
    CollectionType collection,
    String date
) {}
