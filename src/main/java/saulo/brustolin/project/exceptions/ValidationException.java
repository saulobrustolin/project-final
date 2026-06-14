package saulo.brustolin.project.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {

    private final HttpStatus status;
    private final String field;

    public ValidationException(HttpStatus status, String field, String message) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getField() {
        return field;
    }
}
