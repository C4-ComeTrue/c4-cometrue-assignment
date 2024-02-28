package org.c4marathon.assignment.domain.order.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.global.constant.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_tbl")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id", unique = true, nullable = false, updatable = false, columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "status", columnDefinition = "VARCHAR(20)")
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "consumer_id", nullable = false)
	private Consumer consumer;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "delivery_id", nullable = false)
	private Delivery delivery;

	@Builder
	public Order(OrderStatus orderStatus, Consumer consumer, Delivery delivery) {
		this.orderStatus = orderStatus;
		this.consumer = consumer;
		this.delivery = delivery;
	}

	public void updateOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}
}
