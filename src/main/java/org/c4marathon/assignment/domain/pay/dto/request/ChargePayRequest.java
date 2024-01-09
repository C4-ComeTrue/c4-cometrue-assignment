package org.c4marathon.assignment.domain.pay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChargePayRequest(
	@NotNull(message = "amount is null")
	@Min(value = 0, message = "amount is less than zero")
	Long amount
) {
}
