package saulo.brustolin.project.services;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.budgets.BudgetResponseDTO;
import saulo.brustolin.project.dtos.budgets.CreateBudgetDTO;
import saulo.brustolin.project.dtos.budgets.UpdateBudgetDTO;
import saulo.brustolin.project.entities.Budget;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.mappers.BudgetMapper;
import saulo.brustolin.project.repositories.BudgetRepository;

@Service
@AllArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    @Cacheable(value = "budgetsList", key = "#user.id")
    public List<BudgetResponseDTO> getAll(User user) {
        List<Budget> budgets = budgetRepository.findAllByUserId(user.getId());

        return budgets.stream().map(BudgetResponseDTO::fromEntity).toList();
    }

    @Cacheable(value = "budgets", key = "#budgetId")
    public BudgetResponseDTO get(User user, String budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Objetivo não encontrado"));

        return new BudgetResponseDTO(
                budgetId,
                budget.getDescription(),
                budget.getTarget(),
                budget.getBalance(),
                budget.getCreatedAt());
    }

    @Caching(evict = {
            @CacheEvict(value = "budgets", key = "#budgetId"),
            @CacheEvict(value = "budgetsList", key = "#user.id")
    })
    public void delete(User user, String budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Objetivo não encontrado"));

        if (!budget.getUserId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.UNAUTHORIZED, "Esse objetivo é privado");
        }

        budgetRepository.deleteById(budgetId);
    }

    @Transactional
    @Caching(put = {
        @CachePut(value = "budgets", key = "#budgetId")
    }, evict = {
        @CacheEvict(value = "budgetsList", key = "#user.id"),
    })
    public BudgetResponseDTO update(
            User user,
            String budgetId,
            UpdateBudgetDTO dto) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Objetivo não encontrado"));

        if (!budget.getUserId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.UNAUTHORIZED, "Esse objetivo é privado");
        }

        budgetMapper.updateEntityFromDto(dto, budget);

        budgetRepository.save(budget);

        return BudgetResponseDTO.fromEntity(budget);
    }

    @Transactional
    @CacheEvict(value = "budgetsList", key = "#user.id")
    public void create(
            CreateBudgetDTO dto,
            User user) {
        Budget budget = new Budget(dto.description(), dto.target(), dto.balance(), user.getId());

        budgetRepository.save(budget);
    }
}
