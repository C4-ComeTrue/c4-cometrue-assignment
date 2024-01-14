package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Refund {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long refundId;

	@OneToOne
	private Order order;

	@OneToOne
	private Shipment shipment;

	@OneToOne
	private Member customer;

	@OneToOne
	private Member seller;

	@Enumerated(EnumType.STRING)
	private RefundStatus refundStatus;

	private LocalDateTime refundRequestedDate;

	private LocalDateTime refundCompletedDate;
}
