package org.c4marathon.assignment.settlement.dto;

import java.util.List;

import org.c4marathon.assignment.settlement.domain.Settlement;

public record SettlementResponse(
	Long settlementId,
	String requestAccountNumber,
	int totalAmount,
	List<SettlementDetailInfo> members
) {

	public static List<SettlementResponse> fromEntities(List<Settlement> settlements) {
		return settlements.stream()
			.map(SettlementResponse::fromEntity)
			.toList();
	}

	public static SettlementResponse fromEntity(Settlement settlement) {
		return new SettlementResponse(
			settlement.getId(),
			settlement.getRequestAccountNumber(),
			settlement.getTotalAmount(),
			settlement.getSettlementDetails().stream()
				.map(SettlementDetailInfo::fromEntity)
				.toList()
		);
	}
}
