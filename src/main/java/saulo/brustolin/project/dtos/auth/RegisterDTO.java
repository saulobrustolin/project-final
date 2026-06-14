package saulo.brustolin.project.dtos.auth;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterDTO(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank @CPF String cpf,
    @NotBlank String password,
    @NotBlank String confirmPassword
) {}
