package org.c4marathon.assignment.account.domain;

import org.c4marathon.assignment.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class SavingProduct extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "saving_product_id")
	private Long id;

	@Column(nullable = false)
	private double rate;

	@Enumerated(EnumType.STRING)
	private SavingProductType type;

	@Column(nullable = false)
	private int termMonths; // 만기 기간 (개월 단위)

	@Builder
	private SavingProduct(double rate, SavingProductType type, int termMonths) {
		this.rate = rate;
		this.type = type;
		this.termMonths = termMonths;
	}

	public static SavingProduct create(double rate, SavingProductType type, int termMonths) {
		return SavingProduct.builder()
			.rate(rate)
			.type(type)
			.termMonths(termMonths)
			.build();
	}
}
