package org.c4marathon.assignment.bankaccount.dto.request;

import jakarta.validation.constraints.Positive;

public record SendMoneyRequestDto(
	@Positive(message = "올바른 계좌 정보를 보내주세요.")
	long accountPk,
	@Positive(message = "이체는 1원 이상부터 가능합니다.")
	int money
) {
}
