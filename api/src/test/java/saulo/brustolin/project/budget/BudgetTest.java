package saulo.brustolin.project.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;

import saulo.brustolin.project.dtos.budgets.BudgetResponseDTO;
import saulo.brustolin.project.dtos.budgets.CreateBudgetDTO;
import saulo.brustolin.project.dtos.budgets.UpdateBudgetDTO;
import saulo.brustolin.project.entities.Budget;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.mappers.BudgetMapper;
import saulo.brustolin.project.repositories.BudgetRepository;
import saulo.brustolin.project.services.BudgetService;
import saulo.brustolin.shared.dtos.BudgetEvent;

@ExtendWith(MockitoExtension.class)
public class BudgetTest {
    
    @InjectMocks
    private BudgetService budgetService;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private User user;
    private User alternativeUser;

    @BeforeEach
    void setUp() {
        user = new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$");
        user.setId("id-1");
        alternativeUser = new User("Miguel", "miguel@gmail.com", "056.757.920-40", "senha123$");
        alternativeUser.setId("id-2");
    }

    @Test
    void getAll_ShouldReturnListOfBudgetResponseDTO() {
        List<Budget> budgets = List.of(
            new Budget("viagem europa", 3000000, 50000, user.getId()),
            new Budget("casa", 60000000, 12000000, user.getId())
        );
        when(budgetRepository.findAllByUserId(user.getId())).thenReturn(budgets);

        List<BudgetResponseDTO> result = budgetService.getAll(user);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).description()).isEqualTo("viagem europa");
        verify(budgetRepository).findAllByUserId(user.getId());
    }

    @Test
    void get_ShouldReturnBudgetResponseDTO_WhenBudgetExists() {
        Budget budget = new Budget("reserva de emergência", 60000000, 12000000, user.getId());
        when(budgetRepository.findById("id-1")).thenReturn(Optional.of(budget));

        BudgetResponseDTO result = budgetService.get(user, "id-1");

        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("reserva de emergência");
    }

    @Test
    void get_ShouldThrowNotFound_WhenBudgetDoesNotExist() {
        when(budgetRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.get(user, "invalid-id"))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void create_ShouldSaveBudgetAndPublishEvent() {
        CreateBudgetDTO dto = new CreateBudgetDTO("notebook", 500000, 100000);
        Budget savedBudget = new Budget(dto.description(), dto.target(), dto.balance(), user.getId());
        
        when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);

        Budget result = budgetService.create(dto, user);

        assertThat(result).isNotNull();
        verify(budgetRepository).save(any(Budget.class));
        verify(rabbitTemplate).convertAndSend(anyString(), eq("budget.created"), any(BudgetEvent.class));
    }

    @Test
    void update_ShouldModifyBudgetAndPublishEvent_WhenUserIsOwner() {
        Budget existingBudget = new Budget("viagem europa", 3000000, 50000, user.getId());
        UpdateBudgetDTO dto = new UpdateBudgetDTO("investimento", 10000000, 2000000);
        
        when(budgetRepository.findById("id-1")).thenReturn(Optional.of(existingBudget));

        budgetService.update(user, "id-1", dto);

        verify(budgetMapper).updateEntityFromDto(dto, existingBudget);
        verify(budgetRepository).save(existingBudget);
        verify(rabbitTemplate).convertAndSend(anyString(), eq("budget.target"), any(BudgetEvent.class));
    }

    @Test
    void update_ShouldThrowUnauthorized_WhenUserIsNotOwner() {
        Budget existingBudget = new Budget("viagem europa", 3000000, 50000, alternativeUser.getId());
        UpdateBudgetDTO dto = new UpdateBudgetDTO("tentando mudar", null, null);
        
        when(budgetRepository.findById("id-1")).thenReturn(Optional.of(existingBudget));

        assertThatThrownBy(() -> budgetService.update(user, "id-1", dto))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);

        verify(budgetRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(BudgetEvent.class));
    }

    @Test
    void delete_ShouldRemoveBudget_WhenUserIsOwner() {
        Budget existingBudget = new Budget("temporário", 3000000, 50000, user.getId());
        when(budgetRepository.findById("id-1")).thenReturn(Optional.of(existingBudget));

        budgetService.delete(user, "id-1");

        verify(budgetRepository).deleteById("id-1");
    }

    @Test
    void delete_ShouldThrowUnauthorized_WhenUserIsNotOwner() {
        Budget existingBudget = new Budget("todo meu", 3000000, 50000, alternativeUser.getId());
        when(budgetRepository.findById("id-1")).thenReturn(Optional.of(existingBudget));

        assertThatThrownBy(() -> budgetService.delete(user, "id-1"))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);

        verify(budgetRepository, never()).deleteById(anyString());
    }
}
