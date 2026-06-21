package saulo.brustolin.project.entities;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;

@Document(collection = "VerificationCodes")
@AllArgsConstructor
public class VerificationCode {

    @Id
    private String id;
    private String code;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    public String getUserId() { return id; }
    public void setUserId(String userId) { this.id = userId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
