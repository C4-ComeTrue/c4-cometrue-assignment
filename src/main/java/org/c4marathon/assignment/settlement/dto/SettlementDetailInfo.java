package org.c4marathon.assignment.settlement.dto;

import org.c4marathon.assignment.settlement.domain.SettlementDetail;

public record SettlementDetailInfo(
	Long settlementDetailId,
	String accountNumber,
	int amount
) {

	public static SettlementDetailInfo fromEntity(SettlementDetail detail) {
		return new SettlementDetailInfo(
			detail.getId(),
			detail.getAccountNumber(),
			detail.getAmount()
		);
	}
}
