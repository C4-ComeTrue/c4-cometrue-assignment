package org.c4marathon.assignment.settlement.domain;

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

	private String accountNumber;

	private int amount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_id", nullable = false)
	private Settlement settlement;

	@Builder
	private SettlementDetail(Settlement settlement, String accountNumber, int amount) {
		this.settlement = settlement;
		this.accountNumber = accountNumber;
		this.amount = amount;
	}

	public static SettlementDetail create(Settlement settlement, String accountNumber, int amount) {
		SettlementDetail settlementDetail = SettlementDetail.builder()
			.accountNumber(accountNumber)
			.amount(amount)
			.build();
		settlement.addSettlementDetail(settlementDetail);
		return settlementDetail;
	}

	public void setSettlement(Settlement settlement) {
		this.settlement = settlement;
	}
}
