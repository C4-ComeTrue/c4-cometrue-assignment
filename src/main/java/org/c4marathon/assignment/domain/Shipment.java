package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Shipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shipment_pk")
	private Long shipmentPk;

	@Column(name = "courier")
	private String courier;

	@Column(name = "tracking_number")
	private String trackingNumber;

	@Column(name = "registered_date")
	private LocalDateTime registeredDate;

	@Column(name = "completed_date")
	private LocalDateTime completedDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ord")
	private Order order;

}
