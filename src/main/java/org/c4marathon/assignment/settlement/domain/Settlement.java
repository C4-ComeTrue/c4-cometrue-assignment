package org.c4marathon.assignment.settlement.domain;

import static jakarta.persistence.FetchType.*;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "settlement_id")
	private Long id;

	private Long requestAccountId; //정산 요청한 계좌 ID

	private int totalAmount;

	@Enumerated(EnumType.STRING)
	private SettlementType type;

	@OneToMany(mappedBy = "settlement", fetch = LAZY, cascade = CascadeType.ALL)
	private List<SettlementDetail> settlementDetails = new ArrayList<>();

	@Builder
	private Settlement(Long requestAccountId, int totalAmount, SettlementType type) {
		this.requestAccountId = requestAccountId;
		this.totalAmount = totalAmount;
		this.type = type;
	}

	public static Settlement create(Long requestAccountId, int totalAmount, SettlementType type) {
		return Settlement.builder()
			.requestAccountId(requestAccountId)
			.totalAmount(totalAmount)
			.type(type)
			.build();
	}

	public void addSettlementDetail(SettlementDetail settlementDetail) {
		this.settlementDetails.add(settlementDetail);
		settlementDetail.setSettlement(this);

	}
}
