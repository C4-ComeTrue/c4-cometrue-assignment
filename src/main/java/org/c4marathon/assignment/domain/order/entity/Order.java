package org.c4marathon.assignment.domain.order.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
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

	@Column(name = "used_point", columnDefinition = "BIGINT DEFAULT 0")
	private Long usedPoint;

	@Column(name = "earned_point", columnDefinition = "BIGINT DEFAULT 0")
	private Long earnedPoint;

	@Column(name = "total_amount", columnDefinition = "BIGINT DEFAULT 0")
	private Long totalAmount;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "consumer_id", nullable = false)
	private Consumer consumer;

	@Column(name = "delivery_id", columnDefinition = "BIGINT")
	private Long deliveryId;

	@Builder
	public Order(OrderStatus orderStatus, Consumer consumer, long usedPoint) {
		this.orderStatus = orderStatus;
		this.consumer = consumer;
		this.usedPoint = usedPoint;
	}

	public void updateOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void updateDeliveryId(Long deliveryId) {
		this.deliveryId = deliveryId;
	}

	public void updateEarnedPoint(long earnedPoint) {
		this.earnedPoint = earnedPoint;
	}

	public void updateTotalAmount(long totalAmount) {
		this.totalAmount = totalAmount;
	}
}
