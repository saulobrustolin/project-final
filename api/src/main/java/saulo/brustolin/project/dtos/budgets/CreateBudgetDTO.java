package saulo.brustolin.project.dtos.budgets;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateBudgetDTO(
    @NotBlank(message = "A descrição não pode ser vazia") @Size(min = 1, message = "A descrição não pode ser vazia") String description,
    @NotNull(message = "O valor do objetivo é obrigatório") @Positive(message = "O valor do objetivo precisa ser positivo") Integer target,
    @NotNull(message = "O valor do saldo é obrigatório") @Min(0) Integer balance
) {}
