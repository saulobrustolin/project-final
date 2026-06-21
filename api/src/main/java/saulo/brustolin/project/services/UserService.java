package saulo.brustolin.project.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.configurations.RabbitMQConfig;
import saulo.brustolin.project.dtos.budgets.BudgetResponseDTO;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.users.ResumeUserDTO;
import saulo.brustolin.project.dtos.users.UpdateUserDTO;
import saulo.brustolin.shared.dtos.UserEvent;
import saulo.brustolin.shared.dtos.VerificationCodeEvent;
import saulo.brustolin.shared.entities.TransactionType;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.exceptions.ValidationException;
import saulo.brustolin.project.mappers.UserMapper;
import saulo.brustolin.project.repositories.UserRepository;
import saulo.brustolin.project.utils.CodeGenerator;

@Service
@AllArgsConstructor
public class UserService {

    private final TransactionService transactionService;
    private final UserMapper userMapper;
    private final BudgetService budgetService;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;
    private final RabbitTemplate rabbitTemplate;
    private final VerificationCodeService verificationCodeService;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "users", key = "#user.id + '-' + #from + '-' + #to")
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
    @CachePut(value = "users", key = "#result.id")
    public User update(User user, UpdateUserDTO dto) {
        Boolean passwordMatch = passwordEncoder.matches(dto.currentPassword(), user.getPassword());
        if (!passwordMatch) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "currentPassword", "A senha atual não é a mesma da sua conta");
        }
        boolean isValid = verificationCodeService.validateCode(user.getId(), dto.code());
        if (!isValid) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "code", "O código de verificação é inválido.");
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            if (!dto.password().equals(dto.confirmPassword())) {
                throw new ValidationException(HttpStatus.BAD_REQUEST, "confirmPassword", "As senhas não coincidem.");
            }
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        userMapper.updateEntityFromDto(dto, user);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            "user.updated",
            new UserEvent(user.getName(), user.getEmail())
        );

        return userRepository.save(user);
    }

    public void sendCode(User user) {
        String code = codeGenerator.generateNumberCode();

        verificationCodeService.saveCode(user.getId(), code);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            "user.verification-code",
            new VerificationCodeEvent(user.getName(), code, user.getEmail())
        );
    }
}
