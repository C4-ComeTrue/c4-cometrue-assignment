package org.c4marathon.assignment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentPk;

	@ManyToOne
	private Member member;

	@Enumerated(EnumType.STRING)
	private ChargeType valueType; // 지출, 충전 구분

	private Integer value;

	public Payment(Member member, ChargeType valueType, Integer value) {
		this.member = member;
		this.valueType = valueType;
		this.value = value;
	}
}
