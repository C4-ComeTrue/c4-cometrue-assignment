package org.c4marathon.assignment.transaction.dto;

import org.c4marathon.assignment.transaction.domain.TransactionSearchOption;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionGetRequest(
	@NotNull
	TransactionSearchOption option,

	String pageToken,

	@Positive
	@Max(100)
	int count
) {
}
