package saulo.brustolin.project.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

public record AuthenticationDTO(
    @NotBlank @Email String email,
    @NotNull String password
) {}
