package org.c4marathon.assignment.bankaccount.dto.response;

import lombok.Builder;

@Builder
public record SavingAccountResponseDto(
	long accountPk,
	long savingMoney,
	long rate,
	String productName
) {
}
