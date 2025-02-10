package org.c4marathon.assignment.domain;

import java.util.List;

import org.c4marathon.assignment.domain.enums.SettlementType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement")
@Getter
@NoArgsConstructor
public class Settlement extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "settlement_id")
	private Long id;

	@Column(name = "request_account_id", nullable = false)
	private Long requestAccountId;

	@Column(name = "total_amount", nullable = false)
	private int totalAmount;

	@Column(name = "people", nullable = false)
	private int people;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private SettlementType type;

	@OneToMany(mappedBy = "settlement", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<SettlementMemebr> settlementMemebrs;

	public Settlement(Long id, Long requestAccountId, int totalAmount, int people, SettlementType type,
		List<SettlementMemebr> settlementMemebrs) {
		this.id = id;
		this.requestAccountId = requestAccountId;
		this.totalAmount = totalAmount;
		this.people = people;
		this.type = type;
		this.settlementMemebrs = settlementMemebrs;
	}
}
