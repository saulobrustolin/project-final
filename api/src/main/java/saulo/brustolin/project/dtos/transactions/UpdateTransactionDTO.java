package saulo.brustolin.project.dtos.transactions;

import java.time.Instant;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import saulo.brustolin.shared.entities.CollectionType;
import saulo.brustolin.shared.entities.TransactionType;

public record UpdateTransactionDTO(
    @Size(min = 1, message = "A descrição não pode ser vazia") String description,
    @Positive Integer amount,
    TransactionType type,
    CollectionType collection,
    Instant date
) {}
