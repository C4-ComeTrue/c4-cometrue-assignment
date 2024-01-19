package org.c4marathon.assignment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_pk")
	private Long paymentPk;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member")
	private Member member;

	@Column(name = "value_type")
	@Enumerated(EnumType.STRING)
	private ChargeType valueType; // 지출, 충전 구분

	@Column(name = "value")
	private Integer value;

	public Payment(Member member, ChargeType valueType, Integer value) {
		this.member = member;
		this.valueType = valueType;
		this.value = value;
	}
}
