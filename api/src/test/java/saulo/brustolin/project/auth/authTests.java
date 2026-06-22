package saulo.brustolin.project.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import saulo.brustolin.project.dtos.auth.AuthenticationDTO;
import saulo.brustolin.project.dtos.auth.RegisterDTO;
import saulo.brustolin.project.entities.User;
import saulo.brustolin.project.exceptions.ErrorException;
import saulo.brustolin.project.exceptions.ValidationException;
import saulo.brustolin.project.repositories.UserRepository;
import saulo.brustolin.project.services.AuthenticationService;
import saulo.brustolin.project.services.TokenService;
import saulo.brustolin.project.utils.CookieUtil;

@ExtendWith(MockitoExtension.class)
public class authTests {

    @Mock
    private UserRepository repository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationService service;

    @Test
    void registerSuccessTest() {
        RegisterDTO dto = new RegisterDTO("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$", "senha123$");

        Mockito.when(repository.existsByEmail(dto.email())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(dto.password())).thenReturn("crypto-password");
        Mockito.when(tokenService.generateToken(dto.email())).thenReturn("jwt-token");

        Mockito.doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(0);
            String tok = invocation.getArgument(1);
            resp.addCookie(new Cookie("token", tok));
            return null;
        }).when(cookieUtil).addingCookie(Mockito.any(), Mockito.anyString());

        service.register(dto, response);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        Mockito.verify(response).addCookie(cookieCaptor.capture());

        Cookie cookieCap = cookieCaptor.getValue();
        assertEquals("token", cookieCap.getName());
        assertNotNull(cookieCap.getValue());
    }

    @Test
    void loginSuccessTest() {
        // assert
        AuthenticationDTO dto = new AuthenticationDTO("pedro@gmail.com", "senha123$");
        User user = new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$");

        Mockito.when(repository.findByEmailAndIsActiveTrue(dto.email())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
        Mockito.when(tokenService.generateToken(user.getEmail())).thenReturn("fake-jwt-token");

        Mockito.doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(0);
            String tok = invocation.getArgument(1);
            resp.addCookie(new Cookie("token", tok));
            return null;
        }).when(cookieUtil).addingCookie(Mockito.any(), Mockito.anyString());

        // act
        service.authenticate(dto, response);

        // assert
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        Mockito.verify(response).addCookie(cookieCaptor.capture());

        Cookie cookieCap = cookieCaptor.getValue();
        assertEquals("token", cookieCap.getName());
        assertNotNull(cookieCap.getValue());
    }

    @Test
    void loginUserNotFound() {
        // assert
        AuthenticationDTO dto = new AuthenticationDTO("pedro@gmail.com", "senha123$");

        Mockito.when(repository.findByEmailAndIsActiveTrue(dto.email())).thenReturn(Optional.empty());

        // assert & act
        ErrorException exception = assertThrows(ErrorException.class, () -> service.authenticate(dto, response));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void loginInvalidPassword() {
        AuthenticationDTO dto = new AuthenticationDTO("pedro@gmail.com", "senha123$");
        User user = new User("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123#");

        Mockito.when(repository.findByEmailAndIsActiveTrue(dto.email())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        ErrorException exception = assertThrows(ErrorException.class, () -> service.authenticate(dto, response));

        assertEquals("E-mail ou senha incorreta", exception.getMessage());
    }

    @Test
    void registerUserExists() {
        // assert
        RegisterDTO dto = new RegisterDTO("Pedro", "pedro@gmail.com", "912.287.480-12", "senha123$", "senha123$");

        Mockito.when(repository.existsByEmail(dto.email())).thenReturn(true);

        // assert & act
        ValidationException exception = assertThrows(ValidationException.class, () -> service.register(dto, response));

        assertEquals("Já existe um usuário com este e-mail", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void registerDiffPassword() {
        // assert
        RegisterDTO dto = new RegisterDTO("Pedro", "pedro@gmail.com", "912.287.480-12", "senha", "senha123$");

        Mockito.when(repository.existsByEmail(dto.email())).thenReturn(false);

        // assert & act
        ErrorException exception = assertThrows(ErrorException.class, () -> service.register(dto, response));

        assertEquals("As senhas não são iguais", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}
