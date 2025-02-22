package org.c4marathon.assignment.account.dto;

import org.c4marathon.assignment.account.domain.SavingProductType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SavingAccountCreateRequest(
	@NotNull
	SavingProductType type,

	@NotNull
	@Positive
	Long savingProductId,

	@Positive
	long depositAmount
) {
}
