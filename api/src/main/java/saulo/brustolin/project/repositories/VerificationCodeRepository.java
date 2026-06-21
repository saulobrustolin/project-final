package saulo.brustolin.project.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import saulo.brustolin.project.entities.VerificationCode;

public interface VerificationCodeRepository extends MongoRepository<VerificationCode, String> {
    Optional<VerificationCode> findByIdAndCode(String userId, String code);
}
