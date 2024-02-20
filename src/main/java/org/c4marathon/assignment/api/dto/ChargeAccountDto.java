package org.c4marathon.assignment.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ChargeAccountDto {

	public record Req(
		@NotNull(message = "계좌 ID는 null이 될 수 없습니다.") Long accountId,

		@NotNull(message = "충전 금액은 null이 될 수 없습니다.")
		@Positive(message = "충전 금액은 1 이상이여야 합니다.")
		Integer amount
	) {
	}

	public record Res(
		long totalAmount
	) {
	}
}
