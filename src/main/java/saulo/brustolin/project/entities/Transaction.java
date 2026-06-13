package saulo.brustolin.project.entities;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Document(collection = "Transactions")
@RequiredArgsConstructor
public class Transaction {
    
    @Id
    private String id;
    private final String description;
    private final Integer amount;
    private final String userId;
    private final TransactionType type;
    private final CollectionType collection;
    private final Instant date;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
