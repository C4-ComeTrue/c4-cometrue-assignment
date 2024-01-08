package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Order {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderPk;

	@ManyToOne
	private Member customer;

	@OneToOne
	private Payment payment;

	private LocalDateTime orderDate;

	@OneToOne
	private Shipment shipment;

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

}
