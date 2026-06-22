package saulo.brustolin.project.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

import saulo.brustolin.project.dtos.transactions.CreateTransactionDTO;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.transactions.UpdateTransactionDTO;
import saulo.brustolin.project.entities.Transaction;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.repositories.TransactionRepository;
import saulo.brustolin.project.repositories.UserRepository;
import saulo.brustolin.project.services.TransactionService;
import saulo.brustolin.shared.dtos.TransactionEvent;
import saulo.brustolin.shared.entities.CollectionType;
import saulo.brustolin.shared.entities.TransactionType;

@SpringBootTest
public class TransactionIntegrationTests {
    
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @SpyBean
    private RabbitTemplate rabbitTemplate;

    private User user;
    private User alternativeUser;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$"));
        alternativeUser = userRepository.save(new User("Miguel", "miguel@gmail.com", "056.757.920-40", "senha123$"));
    }

    @Test
    void create_ShouldSaveSimpleIncomeAndPublishEventAndUpdateUserBalance() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                "salário",
                500000,
                null,
                new CollectionType("Trabalho", "BriefcaseBusiness"),
                TransactionType.INCOME,
                Instant.now()
        );

        transactionService.createTransaction(user, dto);

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getBalance()).isEqualTo(500000);

        ZoneId hour = ZoneId.systemDefault();
        List<Transaction> transactions = transactionRepository.findAllByUserIdAndDateBetween(
                user.getId(),
                LocalDate.now(hour).atStartOfDay(hour).toInstant(),
                LocalDate.now(hour).atTime(LocalTime.MAX).atZone(hour).toInstant()
        );
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getDescription()).isEqualTo("salário");

        verify(rabbitTemplate).convertAndSend(anyString(), eq("transaction.created"), any(TransactionEvent.class));
    }

    @Test
    void create_ShouldCreateSubdivisionsInsteadOfSingleTransaction() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                "seguro anual",
                120000,
                3,
                new CollectionType("Casa", "House"),
                TransactionType.EXPENSE,
                Instant.now()
        );

        transactionService.createTransaction(user, dto);

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getBalance()).isEqualTo(-120000);

        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(3);
        assertThat(transactions.get(0).getDescription()).isEqualTo("1 de 3 | seguro anual");
        assertThat(transactions.get(0).getAmount()).isEqualTo(40000);
        assertThat(transactions.get(1).getDescription()).isEqualTo("2 de 3 | seguro anual");
        assertThat(transactions.get(2).getDescription()).isEqualTo("3 de 3 | seguro anual");
    }

    @Test
    void get_ShouldReturnResponseDTO_WhenTransactionExistsAndBelongsToUser() {
        Transaction tx = transactionRepository.save(new Transaction(
                "mercado",
                40000,
                user.getId(),
                TransactionType.EXPENSE,
                new CollectionType("Mercado", "ShoppingBasket"),
                Instant.now()
        ));

        TransactionResponseDTO response = transactionService.getTransaction(user, tx.getId());

        assertThat(response).isNotNull();
        assertThat(response.description()).isEqualTo("mercado");
    }

    @Test
    void get_ShouldThrowNotFound_WhenDoesNotExistOrBelongsToOtherUser() {
        Transaction tx = transactionRepository.save(new Transaction(
                "conta de água",
                6000,
                alternativeUser.getId(),
                TransactionType.EXPENSE,
                new CollectionType("Água", "Droplet"),
                Instant.now()
        ));

        assertThatThrownBy(() -> transactionService.getTransaction(user, tx.getId()))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void update_ShouldModifyAmountAndAdjustUserBalanceForExpense() {
        Transaction tx = transactionRepository.save(new Transaction(
                "conta de luz",
                20000,
                user.getId(),
                TransactionType.EXPENSE,
                new CollectionType("Luz", "Zap"),
                Instant.now()
        ));
        UpdateTransactionDTO dto = new UpdateTransactionDTO(
                "luz",
                15000,
                null,
                TransactionType.EXPENSE,
                new CollectionType("Luz", "Zap"),
                Instant.now()
        );

        transactionService.updateTransaction(user, tx.getId(), dto);

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getBalance()).isEqualTo(5000);

        Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
        assertThat(updatedTx.getAmount()).isEqualTo(15000);

        verify(rabbitTemplate).convertAndSend(anyString(), eq("transaction.updated"), any(TransactionEvent.class));
    }

    @Test
    void delete_ShouldRemoveFromDatabaseAndRevertUserBalance() {
        Transaction tx = transactionRepository.save(new Transaction(
                "bônus",
                1500000,
                user.getId(),
                TransactionType.INCOME,
                new CollectionType("Trabalho", "BriefcaseBusiness"),
                Instant.now()
        ));
        
        transactionService.deleteTransaction(user, tx.getId());

        assertThat(transactionRepository.findById(tx.getId())).isEmpty();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getBalance()).isEqualTo(-1500000);

        verify(rabbitTemplate).convertAndSend(anyString(), eq("transaction.deleted"), any(TransactionEvent.class));
    }

    @Test
    void get_ShouldReturnTransactionsWithinDateRange() {
        Instant date1 = Instant.parse("2026-06-01T10:00:00Z");
        Instant date2 = Instant.parse("2026-06-15T10:00:00Z");
        Instant date3 = Instant.parse("2026-07-01T10:00:00Z");

        transactionRepository.save(new Transaction("tx 1", 5000, user.getId(), TransactionType.INCOME, new CollectionType("Trabalho", "BriefcaseBusiness"), date1));
        transactionRepository.save(new Transaction("tx 2", 6000, user.getId(), TransactionType.INCOME, new CollectionType("Trabalho", "BriefcaseBusiness"), date2));
        transactionRepository.save(new Transaction("tx 3", 7000, user.getId(), TransactionType.INCOME, new CollectionType("Trabalho", "BriefcaseBusiness"), date3));

        List<TransactionResponseDTO> result = transactionService.getPeriod(
                user, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)
        );

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TransactionResponseDTO::description).containsExactlyInAnyOrder("tx 1", "tx 2");
    }
}
