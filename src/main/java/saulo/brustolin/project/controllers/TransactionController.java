package saulo.brustolin.project.controllers;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.transactions.CreateTransactionDTO;
import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;
import saulo.brustolin.project.dtos.transactions.UpdateTransactionDTO;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.services.TransactionService;

@RestController
@RequestMapping("/transactions")
@AllArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> createTransaction(@RequestBody CreateTransactionDTO dto, @AuthenticationPrincipal User user) {
        transactionService.createTransaction(user, dto);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@AuthenticationPrincipal User user, @PathVariable String transactionId) {
        TransactionResponseDTO transaction = transactionService.getTransaction(user, transactionId);
        
        return ResponseEntity.ok(transaction);
    }

    @GetMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Iterable<TransactionResponseDTO>> getTransactions(
        @AuthenticationPrincipal User user,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth period
    ) {
        Iterable<TransactionResponseDTO> transactions = transactionService.getPeriod(user, period);

        return ResponseEntity.ok(transactions);
    }

    @PatchMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> updateTransaction(
        @AuthenticationPrincipal User user,
        @PathVariable String transactionId,
        @RequestBody UpdateTransactionDTO dto
    ) {
        transactionService.updateTransaction(user, transactionId, dto);
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> deleteTransaction(@AuthenticationPrincipal User user, @PathVariable String transactionId) {
        transactionService.deleteTransaction(user, transactionId);
        
        return ResponseEntity.ok().build();
    }
}
