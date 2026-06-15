package saulo.brustolin.project.entities;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Document(collection = "Transactions")
@RequiredArgsConstructor
public class Transaction {
    
    @Id
    private String id;
    @NonNull private String description;
    @NonNull private Integer amount;
    @NonNull private String userId;
    @NonNull private TransactionType type;
    @NonNull private CollectionType collection;
    @NonNull private Instant date;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
