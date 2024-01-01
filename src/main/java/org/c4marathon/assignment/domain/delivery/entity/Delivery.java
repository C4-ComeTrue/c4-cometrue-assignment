package org.c4marathon.assignment.domain.delivery.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;
import org.c4marathon.assignment.global.constant.DeliveryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_tbl")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Delivery extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "delivery_id", unique = true, nullable = false, updatable = false, columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "address", columnDefinition = "VARCHAR(100)")
	private String address;

	@NotNull
	@Column(name = "invoice_number", columnDefinition = "VARCHAR(100)")
	private String invoiceNumber;

	@NotNull
	@Column(name = "delivery_status", columnDefinition = "VARCHAR(20)")
	@Enumerated(EnumType.STRING)
	private DeliveryStatus deliveryStatus;

	@Builder
	public Delivery(String address, String invoiceNumber) {
		this.address = address;
		this.invoiceNumber = invoiceNumber;
		this.deliveryStatus = DeliveryStatus.BEFORE_DELIVERY;
	}
}
