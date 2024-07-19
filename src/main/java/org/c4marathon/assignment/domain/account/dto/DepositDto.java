package org.c4marathon.assignment.domain.account.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DepositDto {
    public record Req(
            @Positive(message = "송금할 양은 양수이어야합니다.") @NotNull long amount
    ) {
    }

    public record Res(
            long balance
    ) {
    }
}
