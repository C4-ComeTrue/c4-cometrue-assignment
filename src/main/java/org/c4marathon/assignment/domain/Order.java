package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.C;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.context.annotation.EnableMBeanExport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ORD")
@Getter
@Setter
@NoArgsConstructor
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ord_pk")
	private Long orderPk;

	@ManyToOne
	@JoinColumn(name = "customer_pk")
	private Member customer;

	@ManyToOne
	@JoinColumn(name = "seller_pk")
	private Member seller;

	@OneToMany(mappedBy = "order")
	private List<OrderItem> orderItems = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "payment")
	private Payment payment;

	@Column(name = "ord_date")
	private LocalDateTime orderDate;

	@OneToOne
	@JoinColumn(name = "shipment")
	private Shipment shipment;

	@Enumerated(EnumType.STRING)
	@Column(name = "shipment_status")
	private ShipmentStatus shipmentStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "ord_status")
	private OrderStatus orderStatus;

	@ColumnDefault("false")
	@Column(name = "is_refundable" ,columnDefinition = "TINYINT(1)")
	private boolean isRefundable; // 이 주문 내역의 승인여부를 표시합니다.

	@ColumnDefault("false")
	@Column(name = "is_refunded", columnDefinition = "TINYINT(1)")
	private boolean isRefunded; // 이 주문 내역의 승인여부를 표시합니다.

}
