package org.c4marathon.assignment.domain.pay.dto.request;

import jakarta.validation.constraints.Min;

public record ChargePayRequest(
	@Min(value = 0, message = "amount is less than zero")
	long amount
) {
}
