package org.c4marathon.assignment.settlement.dto;

import java.util.List;


public record SettlementResponse(
	Long settlementId,
	Long requestAccountId,
	int totalAmount,
	List<SettlementDetailInfo> members
) {
}
