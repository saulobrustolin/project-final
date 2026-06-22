package saulo.brustolin.project.dtos.transactions;

import java.time.Instant;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import saulo.brustolin.shared.entities.TransactionType;
import saulo.brustolin.shared.entities.CollectionType;

public record CreateTransactionDTO(
    @NotEmpty(message = "A descrição é obrigatória") String description,
    @NotNull(message = "O preço é obrigatório") @Positive(message = "O preço deve ser maior que 0") Integer amount,
    @Positive(message = "O número de parcelas precisa ser um valor positivo") Integer subdivision,
    @NotNull(message = "A coleção é obrigatória") CollectionType collection,
    @NotNull(message = "A transação é obrigatória") TransactionType type,
    @NotNull(message = "A data é obrigatória") Instant date
) {}
