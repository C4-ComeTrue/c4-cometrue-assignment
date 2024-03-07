package org.c4marathon.assignment.bankaccount.dto.request;

import jakarta.validation.constraints.Positive;

public record ChargeMoneyRequestDto(
	@Positive(message = "충전은 1원 이상부터 가능합니다.")
	long money
) {
}
