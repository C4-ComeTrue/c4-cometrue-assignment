package org.c4marathon.assignment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Sales {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long salesPk;

	@ManyToOne
	private Member member;

	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;

	private Integer value;

}
