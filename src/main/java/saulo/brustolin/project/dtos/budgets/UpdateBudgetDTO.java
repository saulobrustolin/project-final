package saulo.brustolin.project.dtos.budgets;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateBudgetDTO(
    @Size(min = 1, message = "A descrição não pode ser vazia") String description,
    @Positive Integer target
) {}
