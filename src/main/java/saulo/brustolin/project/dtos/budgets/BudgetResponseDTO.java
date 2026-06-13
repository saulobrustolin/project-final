package saulo.brustolin.project.dtos.budgets;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import saulo.brustolin.project.entities.Budget;

public record BudgetResponseDTO(
    @NotBlank String budgetId,
    String description,
    @Positive Integer target,
    @NotNull Instant createdAt
) {
    public final static BudgetResponseDTO fromEntity(Budget budget) {
        return new BudgetResponseDTO(
            budget.getId(), 
            budget.getDescription(), 
            budget.getTarget(), 
            budget.getCreatedAt()
        );
    }
}
