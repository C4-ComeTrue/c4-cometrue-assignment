package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Refund {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "refund_pk")
	private Long refundPk;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ord")
	private Order order;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shipment")
	private Shipment shipment;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer")
	private Member customer;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller")
	private Member seller;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment")
	private Payment payment;

	@Column(name = "refund_status")
	@Enumerated(EnumType.STRING)
	private RefundStatus refundStatus;

	@Column(name = "refund_requested_date")
	private LocalDateTime refundRequestedDate;

	@Column(name = "refund_completed_date")
	private LocalDateTime refundCompletedDate;
}
