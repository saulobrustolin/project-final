package saulo.brustolin.project.dtos.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import saulo.brustolin.project.entities.TransactionType;
import saulo.brustolin.project.entities.CollectionType;

public record CreateTransactionDTO(
    String description,
    @NotNull @Positive Integer amount,
    @NotNull CollectionType collection,
    @NotNull TransactionType type
) {}
