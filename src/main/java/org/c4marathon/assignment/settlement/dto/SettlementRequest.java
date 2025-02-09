package org.c4marathon.assignment.settlement.dto;

import java.util.List;

import org.c4marathon.assignment.settlement.entity.SettlementType;

public record SettlementRequest(

	int totalNumber,
	int totalAmount,
	List<Long> accountIds,
	SettlementType type

) {
}
