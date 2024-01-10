package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Shipment {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long shipmentPk;

	private String courier;

	private String trackingNumber;

	private LocalDateTime registerDate;

	private LocalDateTime completedDate;

	@OneToOne
	private Order order;

}
