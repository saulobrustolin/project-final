package saulo.brustolin.project.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import saulo.brustolin.project.entities.Budget;

public interface BudgetRepository extends MongoRepository<Budget, String> {
    List<Budget> findAllByUserId(String userId);
}
