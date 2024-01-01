package org.c4marathon.assignment.domain.orderproduct.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.product.entity.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "order_product_tbl")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderProduct extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_product_id", unique = true, nullable = false, updatable = false, columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "quantity", columnDefinition = "INT")
	private Integer quantity;

	@NotNull
	@Column(name = "amount", columnDefinition = "BIGINT")
	private Long amount;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Builder
	public OrderProduct(Integer quantity, Long amount, Order order, Product product) {
		this.quantity = quantity;
		this.amount = amount;
		this.order = order;
		this.product = product;
	}
}
