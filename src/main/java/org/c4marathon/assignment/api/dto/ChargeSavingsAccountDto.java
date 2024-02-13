package org.c4marathon.assignment.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ChargeSavingsAccountDto {

	public record Req(
		@NotNull(message = "계좌 ID는 null이 될 수 없습니다.") Long accountId,
		@Positive(message = "적금 자유 이체액은 1 이상이여야 합니다") int chargeAmount
	) {
	}

	public record Res(
		long saveAccountAmount
	) {
	}
}
