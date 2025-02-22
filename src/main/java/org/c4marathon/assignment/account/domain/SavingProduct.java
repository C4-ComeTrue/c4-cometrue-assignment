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

	@Builder
	private SavingProduct(double rate, SavingProductType type) {
		this.rate = rate;
		this.type = type;
	}

	public static SavingProduct create(double rate, SavingProductType type) {
		return SavingProduct.builder()
			.rate(rate)
			.type(type)
			.build();
	}

}
