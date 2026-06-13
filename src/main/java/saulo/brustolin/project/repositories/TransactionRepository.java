package saulo.brustolin.project.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import saulo.brustolin.project.entities.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Optional<Transaction> findByIdAndUserId(String id, String userId);
}
