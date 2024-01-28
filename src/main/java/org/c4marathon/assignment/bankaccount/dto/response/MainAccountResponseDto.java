package org.c4marathon.assignment.bankaccount.dto.response;

import lombok.Builder;

@Builder
public record MainAccountResponseDto(
	long accountPk,
	int chargeLimit,
	int money
) {
}
