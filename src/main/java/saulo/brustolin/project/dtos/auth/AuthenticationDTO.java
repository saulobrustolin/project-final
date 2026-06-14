package saulo.brustolin.project.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

public record AuthenticationDTO(
    @NotBlank(message = "O e-mail é obrigatório") @Email(message = "O e-mail é inválido") String email,
    @NotNull(message = "A senha é obrigatória") String password
) {}
