package org.c4marathon.assignment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAccountDto {

	public record Req(
		String name,
		@NotBlank(message = "계좌 번호는 null이거나 비어있을 수 없습니다.") String accountNumber,
		@NotNull(message = "멤버 ID는 null이 될 수 없습니다.") Long memberId
	) {
	}

	public record Res(
		long id
	) {
	}
}
