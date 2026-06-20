package saulo.brustolin.project.utils;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class CodeGenerator {
    
    private static final SecureRandom random = new SecureRandom();

    public String generateNumberCode() {
        int numero = 100000 + random.nextInt(900000);

        return String.valueOf(numero);
    }
}
