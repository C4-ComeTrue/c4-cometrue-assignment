package org.c4marathon.assignment.entity;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "settlement")
public class Settlement extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "settlement_id", nullable = false)
	private Long id;

	@NotNull
	@Column(name = "requester", nullable = false)
	private Long requester;

	@NotNull
	@Column(name = "total_amount", nullable = false)
	private Long totalAmount;

	@Enumerated(EnumType.STRING)
	private SettlementType type;

	@Builder
	public Settlement(Long requester, Long totalAmount, SettlementType type) {
		this.requester = requester;
		this.totalAmount = totalAmount;
		this.type = type;
	}
}
