package saulo.brustolin.project.dtos.users;

import java.util.List;

import saulo.brustolin.project.dtos.transactions.TransactionResponseDTO;

public record ResumeUserDTO(
    Integer balance,
    Integer net_balance,
    List<TransactionResponseDTO> transactions
) {}