package saulo.brustolin.project.dtos.users;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserDTO(
    String name,
    String email,
    @NotEmpty(message = "A senha atual é obrigatória") String currentPassword,
    String password,
    String confirmPassword,
    @NotEmpty(message = "O código é obrigatório") String code
) {}
