package org.c4marathon.assignment.settlement.dto;

public record ReceivedSettlementResponse(
	Long settlementId,
	Long requestAccountId,
	int totalAmount,
	Long myAccountId,
	int mySettlementAmount
) {

}
