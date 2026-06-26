package saulo.brustolin.project.dtos.users;

import java.util.List;

import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;

public record ResumeUserDTO(
    Integer current_balance,
    Integer net_balance,
    Integer credit,
    Integer debit,
    List<TransactionResponseDTO> transactions
) {}