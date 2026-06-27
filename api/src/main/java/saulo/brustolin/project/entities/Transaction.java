package saulo.brustolin.project.entities;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import saulo.brustolin.shared.entities.CollectionType;
import saulo.brustolin.shared.entities.TransactionType;

@Data
@Document(collection = "Transactions")
public class Transaction {

    @Id
    private String id;
    private String description;
    private Integer amount;
    private String userId;
    private TransactionType type;
    private CollectionType collection;
    private Instant date;
    private String groupId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Transaction(
        String description,
        Integer amount,
        String userId,
        TransactionType type,
        CollectionType collection,
        Instant date,
        String groupId
    ) {
        this.description = description;
        this.amount = amount;
        this.userId = userId;
        this.type = type;
        this.collection = collection;
        this.date = date;
        this.groupId = groupId;
    }
}
