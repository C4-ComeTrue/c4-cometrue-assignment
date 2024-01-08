package org.c4marathon.assignment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Payment {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentPk;

	@ManyToOne
	private Member member;

	private String valueType; // 지출, 충전 구분

	private Integer value;

}
