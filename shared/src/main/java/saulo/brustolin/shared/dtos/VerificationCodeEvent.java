package saulo.brustolin.shared.dtos;

public record VerificationCodeEvent(
    String fullName,
    String code,
    String email
) {}
