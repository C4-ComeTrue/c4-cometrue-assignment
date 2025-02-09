package org.c4marathon.assignment.settlement.entity;

import org.c4marathon.assignment.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementDetail extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "settlement_detail_id")
	private Long id;

	private Long accountId;

	private int amount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_id", nullable = false)
	private Settlement settlement;

	@Builder
	private SettlementDetail(Settlement settlement, Long accountId, int amount) {
		this.settlement = settlement;
		this.accountId = accountId;
		this.amount = amount;
	}

	public static SettlementDetail create(Settlement settlement, Long accountId, int amount) {
		SettlementDetail settlementDetail = SettlementDetail.builder()
			.accountId(accountId)
			.amount(amount)
			.build();
		settlement.addSettlementDetail(settlementDetail);
		return settlementDetail;
	}

	public void setSettlement(Settlement settlement) {
		this.settlement = settlement;
	}
}
