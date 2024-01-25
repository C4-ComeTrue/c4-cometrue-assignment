package org.c4marathon.assignment.bankaccount.dto.request;

import jakarta.validation.constraints.Min;

public record SendToSavingRequestDto(
	@Min(value = 1, message = "올바른 계좌 정보를 보내주세요.")
	long accountPk,
	@Min(value = 1, message = "이체는 1원 이상부터 가능합니다.")
	int money
) {
}
