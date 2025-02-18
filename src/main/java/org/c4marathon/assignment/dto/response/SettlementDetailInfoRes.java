package org.c4marathon.assignment.dto.response;

import org.c4marathon.assignment.entity.SettlementDetail;

public record SettlementDetailInfoRes(
	long userId,
	long amount
) {
	public SettlementDetailInfoRes(SettlementDetail settlementDetail) {
		this(
			settlementDetail.getUserId(),
			settlementDetail.getAmount()
		);
	}
}
