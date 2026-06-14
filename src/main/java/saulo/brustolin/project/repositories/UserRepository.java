package saulo.brustolin.project.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;

import saulo.brustolin.project.entities.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmailAndIsActiveTrue(String email);
    UserDetails findByIdAndIsActiveTrue(String id);
    boolean existsByEmail(String email);
}
