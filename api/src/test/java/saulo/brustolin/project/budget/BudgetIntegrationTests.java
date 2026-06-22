package saulo.brustolin.project.budget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

import saulo.brustolin.project.dtos.budgets.BudgetResponseDTO;
import saulo.brustolin.project.dtos.budgets.CreateBudgetDTO;
import saulo.brustolin.project.dtos.budgets.UpdateBudgetDTO;
import saulo.brustolin.project.entities.Budget;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.repositories.BudgetRepository;
import saulo.brustolin.project.repositories.UserRepository;
import saulo.brustolin.project.services.BudgetService;
import saulo.brustolin.shared.dtos.BudgetEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class BudgetIntegrationTests {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @SpyBean
    private RabbitTemplate rabbitTemplate;

    private User user;
    private User alternativeUser;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        
        user = userRepository.save(new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$"));
        alternativeUser = userRepository.save(new User("Miguel", "miguel@gmail.com", "056.757.920-40", "senha123$"));
    }


    @Test
    void getAll_ShouldReturnOnlyBudgetsBelongingToTheUser() {
        budgetRepository.save(new Budget("viagem europa", 3000000, 50000, user.getId()));
        budgetRepository.save(new Budget("casa", 60000000, 12000000, user.getId()));

        List<BudgetResponseDTO> result = budgetService.getAll(user);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BudgetResponseDTO::description).containsExactlyInAnyOrder("viagem europa", "casa");
    }

    @Test
    void get_ShouldReturnBudgetWhenExists() {
        Budget savedBudget = budgetRepository.save(budgetRepository.save(new Budget("viagem europa", 3000000, 50000, alternativeUser.getId())));

        BudgetResponseDTO result = budgetService.get(user, savedBudget.getId());

        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("viagem europa");
    }

    @Test
    void get_ShouldThrowNotFoundWhenBudgetDoesNotExist() {
        assertThatThrownBy(() -> budgetService.get(user, "id-inexistente"))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void create_ShouldSaveBudgetAndPublishCreatedEvent() {
        CreateBudgetDTO dto = new CreateBudgetDTO("notebook", 500000, 0);

        Budget created = budgetService.create(dto, user);

        Optional<Budget> found = budgetRepository.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("notebook");

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                eq("budget.created"),
                any(BudgetEvent.class)
        );
    }

    @Test
    void update_ShouldModifyBudgetAndPublishTargetEvent() {
        Budget savedBudget = budgetRepository.save(new Budget("viagem europa", 3000000, 50000, user.getId()));
        UpdateBudgetDTO updateDto = new UpdateBudgetDTO("reserva emergencial", 50000000, null);

        BudgetResponseDTO updated = budgetService.update(user, savedBudget.getId(), updateDto);

        assertThat(updated.description()).isEqualTo("reserva emergencial");
        assertThat(updated.target()).isEqualTo(50000000);
        assertThat(updated.balance()).isEqualTo(50000);

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                eq("budget.target"),
                any(BudgetEvent.class)
        );
    }

    @Test
    void update_ShouldThrowUnauthorizedWhenBudgetBelongsToAnotherUser() {
        Budget savedBudget = budgetRepository.save(new Budget("viagem europa", 3000000, 50000, alternativeUser.getId()));
        UpdateBudgetDTO updateDto = new UpdateBudgetDTO("espanha", null, null);

        assertThatThrownBy(() -> budgetService.update(user, savedBudget.getId(), updateDto))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);
    }


    @Test
    void delete_ShouldRemoveBudgetFromDatabase() {
        Budget savedBudget = budgetRepository.save(new Budget("deletável", 3000000, 50000, user.getId()));

        budgetService.delete(user, savedBudget.getId());

        Optional<Budget> found = budgetRepository.findById(savedBudget.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void delete_ShouldThrowUnauthorizedWhenDeletingOthersBudget() {
        Budget savedBudget = budgetRepository.save(new Budget("don't touch", 3000000, 50000, alternativeUser.getId()));

        assertThatThrownBy(() -> budgetService.delete(user, savedBudget.getId()))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);
    }
}
