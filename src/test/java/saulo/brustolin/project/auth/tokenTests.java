package saulo.brustolin.project.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import saulo.brustolin.project.services.TokenService;

@ExtendWith(MockitoExtension.class)
public class tokenTests {
    
    @InjectMocks
    private TokenService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "name", "project-name");
        ReflectionTestUtils.setField(service, "secret", "secret-key");
    }

    @Test
    void tokenGenerator() {
        String email = "joao@gmail.com";

        String token = service.generateToken(email);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken() {
        String email = "joao@gmail.com";

        String token = service.generateToken(email);
        String tokenValidated = service.validateToken(token);

        assertNotNull(tokenValidated);
        assertFalse(tokenValidated.isBlank());
        assertEquals(email, tokenValidated);
    }

    @Test
    void invalidTokenVerification() {
        String token = "invalid-token";

        assertNotNull(token);
        assertFalse(token.isBlank());

        String tokenValidated = service.validateToken(token);

        assertTrue(tokenValidated.isBlank());
    }
}
