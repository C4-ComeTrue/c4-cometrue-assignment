package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.NotNull;

public record RechargeAccountRequestDto(
    @NotNull
    Long accountId,
    @NotNull
    Long balance
) {
}
