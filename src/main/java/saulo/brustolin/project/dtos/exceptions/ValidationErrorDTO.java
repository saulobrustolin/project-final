package saulo.brustolin.project.dtos.exceptions;

public record ValidationErrorDTO(
    String field,
    String message
) {}
