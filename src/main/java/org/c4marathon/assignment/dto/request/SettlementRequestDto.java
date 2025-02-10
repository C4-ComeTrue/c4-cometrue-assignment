package org.c4marathon.assignment.dto.request;

import org.c4marathon.assignment.domain.enums.SettlementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SettlementRequestDto(
	long requestAccountId,
	@Positive(message = "양수값으로 입력해주세요.")
	int totalAmount,
	@Positive(message = "양수값으로 입력해주세요.")
	int people,
	@NotBlank
	SettlementType type
) {
}
