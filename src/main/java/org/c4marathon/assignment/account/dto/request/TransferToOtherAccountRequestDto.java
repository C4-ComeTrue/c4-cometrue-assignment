package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TransferToOtherAccountRequestDto(

    @PositiveOrZero
    Long balance,
    @NotNull
    Long receiverAccountId
) {
}
