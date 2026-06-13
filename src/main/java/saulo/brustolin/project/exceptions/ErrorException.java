package saulo.brustolin.project.exceptions;

import org.springframework.http.HttpStatus;

public class ErrorException extends RuntimeException {

    private final HttpStatus status;

    public ErrorException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
