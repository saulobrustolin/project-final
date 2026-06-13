package saulo.brustolin.project.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.budgets.BudgetResponseDTO;
import saulo.brustolin.project.entities.Budget;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.repositories.BudgetRepository;

@Service
@AllArgsConstructor
public class BudgetService {
    
    private final BudgetRepository budgetRepository;

    public List<BudgetResponseDTO> getBudgets(User user) {
        List<Budget> budgets = budgetRepository.findAllByUserId(user.getId());

        return budgets.stream().map(BudgetResponseDTO::fromEntity).toList();
    }

    public BudgetResponseDTO getBudget(User user, String budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Objetivo não encontrado"));

        return new BudgetResponseDTO(
            budgetId, 
            budget.getDescription(), 
            budget.getTarget(), 
            budget.getCreatedAt()
        );
    }

    public void delete(User user, String budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Objetivo não encontrado"));

        if (budget.getUserId() != user.getId()) {
            throw new ErrorException(HttpStatus.UNAUTHORIZED, "Esse objetivo é privado");
        }

        budgetRepository.deleteById(budgetId);
    }
}
