package saulo.brustolin.project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.transaction.CreateTransactionDTO;
import saulo.brustolin.project.dtos.transaction.TransactionResponseDTO;
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
}
