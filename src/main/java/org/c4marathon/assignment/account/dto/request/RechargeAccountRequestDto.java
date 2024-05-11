package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RechargeAccountRequestDto(
    @NotNull
    Long accountId,
    @PositiveOrZero
    Long balance
) {
}
