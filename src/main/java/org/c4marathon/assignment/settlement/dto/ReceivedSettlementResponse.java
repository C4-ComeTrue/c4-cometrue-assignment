package org.c4marathon.assignment.settlement.dto;

import java.util.List;

import org.c4marathon.assignment.settlement.domain.SettlementDetail;

public record ReceivedSettlementResponse(
	Long settlementId,
	String requestAccountNumber,
	int totalAmount,
	String myAccountNumber,
	int mySettlementAmount
) {

	public static List<ReceivedSettlementResponse> fromEntities(List<SettlementDetail> settlementDetails) {
		return settlementDetails.stream()
			.map(ReceivedSettlementResponse::fromEntity)
			.toList();
	}

	public static ReceivedSettlementResponse fromEntity(SettlementDetail detail) {
		return new ReceivedSettlementResponse(
			detail.getSettlement().getId(),
			detail.getSettlement().getRequestAccountNumber(),
			detail.getSettlement().getTotalAmount(),
			detail.getAccountNumber(),
			detail.getAmount()
		);
	}

}
