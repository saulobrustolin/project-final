package saulo.brustolin.project.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import saulo.brustolin.project.dtos.auth.AuthenticationDTO;
import saulo.brustolin.project.dtos.auth.RegisterDTO;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.repositories.UserRepository;
import saulo.brustolin.project.utils.CookieUtil;
import saulo.brustolin.project.entities.User;

@Service
@AllArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;

    public void authenticate(AuthenticationDTO dto, HttpServletResponse response) {
        var userDetails = userRepository.findByEmailAndIsActiveTrue(dto.email())
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!passwordEncoder.matches(dto.password(), userDetails.getPassword())) {
            throw new ErrorException(HttpStatus.UNAUTHORIZED, "Senha incorreta");
        }

        String token = tokenService.generateToken(userDetails.getUsername());

        cookieUtil.addingCookie(response, token);
    }

    public void register(RegisterDTO dto, HttpServletResponse response) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Email já cadastrado");
        }

        if (!dto.password().equals(dto.confirmPassword())) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "As senhas não são iguais");
        }

        var user = new User(dto.name(), dto.email(), dto.cpf(), passwordEncoder.encode(dto.password()));
        userRepository.save(user);

        String token = tokenService.generateToken(user.getEmail());
        cookieUtil.addingCookie(response, token);
    }
}
