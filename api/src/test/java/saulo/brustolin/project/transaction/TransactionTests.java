package saulo.brustolin.project.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;

import saulo.brustolin.project.dtos.transactions.CreateTransactionDTO;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.transactions.UpdateTransactionDTO;
import saulo.brustolin.shared.entities.CollectionType;
import saulo.brustolin.project.entities.Transaction;
import saulo.brustolin.shared.entities.TransactionType;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.mappers.BudgetMapper;
import saulo.brustolin.project.mappers.TransactionMapper;
import saulo.brustolin.project.repositories.TransactionRepository;
import saulo.brustolin.project.repositories.UserRepository;
import saulo.brustolin.project.services.TransactionService;

@ExtendWith(MockitoExtension.class)
public class TransactionTests {
    
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

    @InjectMocks
    private TransactionService service;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private BudgetMapper budgetMapper;

    @Test
    void create_SuccessTransaction() {
        User user = new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$");
        user.setId("babalu");
        user.setBalance(1000);

        CreateTransactionDTO transaction = new CreateTransactionDTO(
            "shopping",
            900,
            null,
            null,
            new CollectionType("Shopping", "cart"), 
            TransactionType.INCOME, 
            Instant.now()
        );

        service.createTransaction(user, transaction);

        assertEquals(1900, user.getBalance());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        Mockito.verify(transactionRepository).save(transactionCaptor.capture());

        Transaction transactionSave = transactionCaptor.getValue();
        assertEquals("shopping", transactionSave.getDescription());
        assertEquals(900, transactionSave.getAmount());
        assertEquals("babalu", transactionSave.getUserId());
    }

    @Test
    void get_SuccessTransaction() {
        User user = new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$");
        user.setId("babalu");
        user.setBalance(1000);

        Transaction transaction = new Transaction(
            "pizza", 
            7000, 
            "babalu", 
            TransactionType.EXPENSE, 
            new CollectionType("Food", "food"), 
            Instant.now()
        );
        transaction.setId("pizza");

        Mockito.when(transactionRepository.findByIdAndUserId(transaction.getId(), transaction.getUserId())).thenReturn(Optional.of(transaction));

        TransactionResponseDTO dto = service.getTransaction(user, transaction.getId());

        assertEquals(transaction.getId(), dto.transactionId());
        assertEquals(transaction.getDescription(), dto.description());
        assertEquals(transaction.getAmount(), dto.amount());
        assertEquals(transaction.getType(), dto.type());
        assertEquals(transaction.getCollection(), dto.collection());
    }

    @Test
    void get_ErrorTransaction() {
        User user = new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$");
        user.setId("babalu");
        user.setBalance(1000);

        ErrorException exception = assertThrows(ErrorException.class, () -> service.getTransaction(user, "babalu"));
        
        assertEquals(exception.getMessage(), "Transação não encontrada");
        assertEquals(exception.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    void update_SuccessChangeAmountTransaction() {
        User user = new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$");
        user.setId("babalu");
        user.setBalance(1000);

        Transaction transaction = new Transaction(
            "pizza", 
            7000,
            "babalu", 
            TransactionType.EXPENSE, 
            new CollectionType("Food", "food"), 
            Instant.now()
        );
        transaction.setId("pizza");

        Mockito.when(transactionRepository.findByIdAndUserId("pizza", user.getId())).thenReturn(Optional.of(transaction));
        
        UpdateTransactionDTO dto = new UpdateTransactionDTO("smash burguer", 5300, null, null, null, Instant.now());
        
        service.updateTransaction(user, "pizza", dto);

        assertEquals(transaction.getAmount(), 5300, "O valor da transação deveria ter sido atualizada");
        assertEquals(transaction.getDescription(), "smash burguer", "O parãmetro de descrição deveria ter sido atualizado");
        assertEquals(user.getBalance(), 2700, "O balance do usuário deveria ter sido atualizado");
    }

    @Test
    void update_NotFoundDeleteTransaction() {
        User user = new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$");

        ErrorException exception = assertThrows(ErrorException.class, () -> service.deleteTransaction(user, "babalu"));

        assertEquals(exception.getStatus(), HttpStatus.NOT_FOUND);
        assertEquals(exception.getMessage(), "Transação não encontrada");
    }
}
