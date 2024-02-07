package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.NotNull;

public record SavingAccountRequestDto(
    @NotNull
    Integer balance,
    @NotNull
    Long receiverAccountId
) {
}
