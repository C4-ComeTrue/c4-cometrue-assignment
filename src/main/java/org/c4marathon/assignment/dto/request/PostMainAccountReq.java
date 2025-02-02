package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.Positive;

public record PostMainAccountReq(
	@Positive(message = "회원 번호는 양수가 되어야 합니다.")
	long userId,

	@Positive(message = "이체 금액은 양수가 되어야 합니다.")
	long amount
) {
}
