package org.c4marathon.assignment.account.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WithdrawRequest(

        @NotNull
        Long receiverAccountId,

        @PositiveOrZero
        long money
) {
}
