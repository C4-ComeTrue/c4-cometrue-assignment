package org.c4marathon.assignment.settlement.dto;

public record ReceivedSettlementResponse(
	Long settlementId,
	String requestAccountNumber,
	int totalAmount,
	String myAccountNumber,
	int mySettlementAmount
) {

}
