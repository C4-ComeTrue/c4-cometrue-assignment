package org.c4marathon.assignment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateChargeLinkedAccountDto {

	public record Req(
		@NotNull(message = "계좌 ID는 null이 될 수 없습니다.") Long accountId,
		@NotBlank(message = "은행 정보는 비어있을 수 없습니다.") String bank,
		@NotBlank(message = "연동 계좌 정보는 비어있을 수 없습니다.") String accountNumber,
		@NotNull(message = "주 계좌 여부는 null이 될 수 없습니다.") boolean isMain
	) {
	}
}
