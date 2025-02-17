package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.Positive;

public record TransferAcceptReq(
	@Positive(message = "회원 번호는 양수가 되어야 합니다.")
	long requester
) {
}
