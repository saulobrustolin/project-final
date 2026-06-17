package saulo.brustolin.project.exceptions;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import saulo.brustolin.project.dtos.exceptions.ErrorDTO;
import saulo.brustolin.project.dtos.exceptions.ValidationErrorDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorDTO>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ValidationErrorDTO> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationErrorDTO(
                        fieldError.getField(), 
                        fieldError.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ErrorDTO> handleError(ErrorException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorDTO(ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<List<ValidationErrorDTO>> handleValidation(ValidationException ex) {
        return ResponseEntity.status(ex.getStatus()).body(List.of(new ValidationErrorDTO(ex.getField(), ex.getMessage())));
    }
}
