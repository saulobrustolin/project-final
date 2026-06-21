package saulo.brustolin.shared.dtos;

public record BudgetEvent(
    String name,
    Integer balance,
    Integer target,
    String email,
    String fullname
) {}