package org.c4marathon.assignment.settlement.dto;

public record SettlementDetailInfo(
	Long settlementDetailId,
	Long accountId,
	int amount
) {
}
