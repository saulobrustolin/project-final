package saulo.brustolin.project.dtos.auth;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
    @NotBlank(message = "O nome é obrigatório") String name,
    @NotBlank(message = "O e-mail é obrigatório") @Email(message = "O e-mail é inválido") String email,
    @NotBlank(message = "O CPF é obrigatório") @CPF(message = "O CPF é inválido") String cpf,
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = ".*[!@#$%^&*(),.?:{}|<>_].*", 
        message = "A senha deve conter pelo menos um caractere especial"
    )
    String password,
    @NotBlank(message = "A senha de confirmação é obrigatória") String confirmPassword
) {}
