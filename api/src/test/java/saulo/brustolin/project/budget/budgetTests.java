package saulo.brustolin.project.budget;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import saulo.brustolin.project.dtos.budgets.CreateBudgetDTO;
import saulo.brustolin.project.entities.Budget;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.services.BudgetService;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class budgetTests {

    @Autowired
    private BudgetService budgetService;

    @Test
    void createSuccessBudget() {
        User user = new User("Pedro", "hehekem459@lidugw.com", "312.151.940-99", "senha123$");
        user.setId("danone");
        CreateBudgetDTO dto = new CreateBudgetDTO("viagem para europa", 3000000, 50000);

        Budget budget = budgetService.create(dto, user);

        assertThat(budget.getDescription()).isEqualTo("viagem para europa");
    }
}
