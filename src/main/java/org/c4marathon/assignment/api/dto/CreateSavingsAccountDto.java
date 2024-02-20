package org.c4marathon.assignment.api.dto;

import org.c4marathon.assignment.domain.SavingsType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateSavingsAccountDto {

	public record Req(
		String name,
		@NotNull(message = "멤버 ID는 null이 될 수 없습니다") Long memberId,
		@Positive(message = "적금 자동 이체액은 1 이상이여야 합니다") int withdrawAmount,
		@NotNull(message = "적금 종류는 null이 될 수 없습니다") SavingsType savingsType
	) {
	}

	public record Res(
		long id
	) {
	}
}
