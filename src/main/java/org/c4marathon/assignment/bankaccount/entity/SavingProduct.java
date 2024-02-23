package org.c4marathon.assignment.bankaccount.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class SavingProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_pk", nullable = false, updatable = false)
	private long productPk;

	@Column(name = "product_name", nullable = false)
	private String productName;

	@Column(name = "product_rate", nullable = false)
	private int productRate;
}
