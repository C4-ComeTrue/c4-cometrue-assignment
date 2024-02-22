package org.c4marathon.assignment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferAccountDto {

	public record Req(
		@NotNull(message = "계좌 ID는 null이 될 수 없습니다.") Long accountId,
		@NotBlank(message = "송금하려는 계좌의 번호는 비어있을 수 없습니다.") String accountNumber,
		@NotNull @Positive(message = "송금은 1원 이상 가능합니다.") Long transferAmount
	) {

	}

	public record Res(
		long totalAmount
	) {

	}
}
