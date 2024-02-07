package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RechargeAccountRequestDto(
    @NotBlank
    Long accountId,
    @NotBlank
    Integer balance
) {
}
