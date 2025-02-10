package org.c4marathon.assignment.dto.request;

import org.c4marathon.assignment.domain.enums.SettlementStatus;
import jakarta.validation.constraints.NotBlank;

public record SettleMemberRequestDto(
	long accountId,
	@NotBlank
	SettlementStatus status
) {}