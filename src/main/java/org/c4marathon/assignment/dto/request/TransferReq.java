package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.Positive;

public record TransferReq(
	@Positive(message = "사용자 번호는 양수가 되어야 합니다.")
	long senderId,

	@Positive(message = "메인 계좌번호는 양수가 되어야 합니다.")
	long receiverMainAccount,

	@Positive(message = "송금 금액은 양수가 되어야 합니다.")
	long amount
) {
}
