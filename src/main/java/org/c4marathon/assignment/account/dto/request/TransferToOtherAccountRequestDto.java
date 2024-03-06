package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransferToOtherAccountRequestDto(

    @NotNull
    Long balance,
    @NotNull
    Long receiverAccountId
) {
}
