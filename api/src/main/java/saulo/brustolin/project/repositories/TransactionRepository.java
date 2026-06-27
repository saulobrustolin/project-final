package saulo.brustolin.project.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import saulo.brustolin.project.entities.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Optional<Transaction> findByIdAndUserId(String id, String userId);
    List<Transaction> findAllByUserIdAndDateBetween(String userId, Instant start, Instant end);
    List<Transaction> findAllByUserIdAndDateLessThan(String userId, Instant end);
    void deleteAllByGroupId(String groupId);
    void deleteAllByGroupIdAndDateGreaterThan(String groupId, Instant date);
}
