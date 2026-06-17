package saulo.brustolin.project.dtos.transactions;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import saulo.brustolin.project.entities.TransactionType;
import saulo.brustolin.project.entities.CollectionType;

public record CreateTransactionDTO(
    String description,
    @NotNull(message = "O preço é obrigatório") @Positive(message = "O preço deve ser maior que 0") Integer amount,
    @NotNull(message = "A coleção é obrigatória") CollectionType collection,
    @NotNull(message = "A transação é obrigatória") TransactionType type,
    @NotNull(message = "A data é obrigatória") Instant date
) {}
