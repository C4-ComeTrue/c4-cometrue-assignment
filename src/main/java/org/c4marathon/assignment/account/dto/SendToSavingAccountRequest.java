package org.c4marathon.assignment.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SendToSavingAccountRequest(

	@NotBlank(message = "적금 계좌 ID는 필수 입력 값입니다.")
	String savingAccountNumber,

	@Positive(message = "잘못된 이체 금액입니다.")
	long money
) {
}

