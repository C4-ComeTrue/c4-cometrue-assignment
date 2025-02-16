package org.c4marathon.assignment.dto.request;

import java.util.List;

import org.c4marathon.assignment.entity.SettlementType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PostSettlementReq(
	@Positive(message = "회원 번호는 양수가 되어야 합니다.")
	long requester,

	@Positive(message = "정산 금액은 양수가 되어야 합니다.")
	long totalAmount,

	@NotNull
	SettlementType type,

	@NotNull
	List<Long> userIds
) {
}
