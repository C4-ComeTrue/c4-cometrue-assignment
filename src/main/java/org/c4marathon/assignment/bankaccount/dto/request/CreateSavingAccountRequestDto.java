package org.c4marathon.assignment.bankaccount.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateSavingAccountRequestDto(
	@NotNull(message = "상품명을 입력해주세요.")
	String productName
) {
}
