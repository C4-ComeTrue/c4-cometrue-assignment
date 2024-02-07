package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SavingAccountRequestDto(
    @NotBlank
    Integer balance,
    @NotBlank
    Long receiverAccountId
) {
}
