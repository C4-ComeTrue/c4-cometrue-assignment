package org.c4marathon.assignment.settlement.dto;

import java.util.List;

import org.c4marathon.assignment.settlement.domain.SettlementType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SettlementRequest(

	@PositiveOrZero
	@Max(value = 100, message = "100명까지 정산이 가능합니다.")
	int totalNumber,

	@PositiveOrZero
	int totalAmount,

	@NotNull(message = "정산 인원을 추가해주세요.")
	List<String> accountNumbers,

	@NotNull(message = "정산 타입을 선택해주세요.")
	SettlementType type

) {
}
