package saulo.brustolin.project.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import saulo.brustolin.project.entities.VerificationCode;
import saulo.brustolin.project.repositories.VerificationCodeRepository;

@Service
@AllArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository repository;
    private static final long TTL_MINUTES = 15;

    public void saveCode(String userId, String code) {
        Instant expiresAt = Instant.now().plus(TTL_MINUTES, ChronoUnit.MINUTES);
        VerificationCode verificationCode = new VerificationCode(userId, code, expiresAt);
        
        repository.save(verificationCode);
    }

    public boolean validateCode(String userId, String inputCode) {
        Optional<VerificationCode> savedCodeOpt = repository.findByUserIdAndCode(userId, inputCode);

        if (savedCodeOpt.isPresent()) {
            VerificationCode savedCode = savedCodeOpt.get();
            
            if (savedCode.getExpiresAt().isAfter(Instant.now())) {
                repository.deleteById(userId);
                return true;
            }
        }
        return false;
    }
}
