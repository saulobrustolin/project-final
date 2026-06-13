package saulo.brustolin.project.services;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.users.ResumeUserDTO;
import saulo.brustolin.project.dtos.users.UpdateUserDTO;
import saulo.brustolin.project.entities.TransactionType;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.mappers.UserMapper;
import saulo.brustolin.project.repositories.UserRepository;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final UserMapper userMapper;

    public UserDetails loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public ResumeUserDTO getResume(User user, YearMonth period) {
        List<TransactionResponseDTO> transactions = transactionService.getPeriod(user, period);

        Integer net_balance = transactions.stream()
            .mapToInt(t -> t.type() == TransactionType.INCOME ? t.amount() : -t.amount())
            .sum();

        return new ResumeUserDTO(
            user.getBalance(),
            net_balance,
            transactions
        );
    }

    @Transactional
    public void updateBalance(User user, Integer amount) {
        user.setBalance(user.getBalance() + amount);
    }

    @Transactional
    public void update(User user, UpdateUserDTO dto) {
        userMapper.updateEntityFromDto(dto, user);
    }
}
