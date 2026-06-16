package saulo.brustolin.project.services;

import java.time.LocalDate;
import java.util.List;

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

    public ResumeUserDTO getResume(User user, LocalDate from, LocalDate to) {
        List<TransactionResponseDTO> transactions = transactionService.getPeriod(user, from, to);

        Integer net_balance = transactions.stream()
            .mapToInt(t -> t.type() == TransactionType.INCOME ? t.amount() : -t.amount())
            .sum();

        List<BudgetResponseDTO> budgets = budgetService.getBudgets(user);

        return new ResumeUserDTO(
            user.getBalance(),
            net_balance,
            transactions,
            budgets
        );
    }

    @Transactional
    public void update(User user, UpdateUserDTO dto) {
        userMapper.updateEntityFromDto(dto, user);

        userRepository.save(user);
    }
}
