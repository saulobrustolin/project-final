package saulo.brustolin.project.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.budgets.BudgetResponseDTO;
import saulo.brustolin.project.dtos.budgets.CreateBudgetDTO;
import saulo.brustolin.project.dtos.budgets.UpdateBudgetDTO;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.services.BudgetService;

@RestController
@RequestMapping(path = "/budgets")
@AllArgsConstructor
public class BudgetController {
    
    private final BudgetService budgetService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<BudgetResponseDTO>> allBudgets(
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(budgetService.getAll(user));
    }

    @PatchMapping(path = "/{budgetId}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<BudgetResponseDTO> budget(
        @AuthenticationPrincipal User user,
        @PathVariable String budgetId,
        @RequestBody @Valid UpdateBudgetDTO dto
    ) {
        budgetService.update(user, budgetId, dto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/{budgetId}", produces = "application/json")
    public ResponseEntity<Void> deleteBudget(
        @AuthenticationPrincipal User user,
        @PathVariable String budgetId
    ) {
        budgetService.delete(user, budgetId);

        return ResponseEntity.ok().build();
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<Void> createBudget(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid CreateBudgetDTO dto
    ) {
        budgetService.create(dto, user);

        return ResponseEntity.ok().build();
    }
}
