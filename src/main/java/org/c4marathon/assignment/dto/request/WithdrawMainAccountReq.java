package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.Positive;

public record WithdrawMainAccountReq(
	@Positive(message = "회원 번호는 양수가 되어야 합니다.")
	long userId,
	@Positive(message = "적금 계좌번호는 양수가 되어야 합니다.")
	long savingsAccount,
	@Positive(message = " 금액은 양수가 되어야 합니다.")
	long amount
) {
}
