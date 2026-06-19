package saulo.brustolin.project.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.budgets.BudgetResponseDTO;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.users.ResumeUserDTO;
import saulo.brustolin.project.dtos.users.UpdateUserDTO;
import saulo.brustolin.project.entities.TransactionType;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.mappers.UserMapper;
import saulo.brustolin.project.repositories.UserRepository;

@Service
@AllArgsConstructor
public class UserService {

    private final TransactionService transactionService;
    private final UserMapper userMapper;
    private final BudgetService budgetService;
    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#user.id")
    public ResumeUserDTO getResume(User user, LocalDate from, LocalDate to) {
        List<TransactionResponseDTO> transactions = transactionService.getPeriod(user, from, to);
        
        Integer credit = transactions.stream()
            .filter(t -> t.type() == TransactionType.EXPENSE)
            .mapToInt(t -> t.amount())
            .sum();
        
        Integer debit = transactions.stream()
            .filter(t -> t.type() == TransactionType.INCOME)
            .mapToInt(t -> t.amount())
            .sum();

        Integer net_balance = debit - credit;

        List<BudgetResponseDTO> budgets = budgetService.getAll(user);

        return new ResumeUserDTO(
            user.getBalance(),
            net_balance,
            credit,
            debit,
            transactions,
            budgets
        );
    }

    @Transactional
    @CachePut(value = "users", key = "#user.id")
    public void update(User user, UpdateUserDTO dto) {
        userMapper.updateEntityFromDto(dto, user);

        userRepository.save(user);
    }
}
