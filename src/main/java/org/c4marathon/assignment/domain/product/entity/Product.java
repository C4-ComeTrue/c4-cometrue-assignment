package org.c4marathon.assignment.domain.product.entity;

import static org.c4marathon.assignment.global.constant.ProductStatus.*;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.global.constant.ProductStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "product_tbl",
	indexes = {
		@Index(name = "product_name_seller_id_idx", columnList = "name,seller_id"),
		@Index(name = "amount_product_id_idx", columnList = "amount, product_id"),
		@Index(name = "amount_desc_product_id_idx", columnList = "amount desc, product_id"),
		@Index(name = "created_at_product_id_idx", columnList = "created_at desc, product_id"),
		@Index(name = "avg_score_desc_product_id_idx", columnList = "avg_score desc, product_id asc"),
		@Index(name = "order_count_desc_product_id_idx", columnList = "order_count desc, product_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id", unique = true, nullable = false, updatable = false, columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "name", columnDefinition = "VARCHAR(100)")
	private String name;

	@NotNull
	@Column(name = "description", columnDefinition = "VARCHAR(500)")
	private String description;

	@NotNull
	@Column(name = "amount", columnDefinition = "BIGINT")
	private Long amount;

	@NotNull
	@Column(name = "stock", columnDefinition = "INT")
	private Integer stock;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seller_id", nullable = false)
	private Seller seller;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", columnDefinition = "VARCHAR(20)")
	private ProductStatus productStatus;

	@NotNull
	@Column(name = "avg_score", columnDefinition = "DECIMAL(5, 4) DEFAULT 0.0000")
	private Double avgScore;

	@NotNull
	@Column(name = "order_count", columnDefinition = "BIGINT DEFAULT 0")
	private Long orderCount;

	@Builder
	public Product(
		String name,
		String description,
		Long amount,
		Integer stock,
		Seller seller
	) {
		this.name = name;
		this.description = description;
		this.amount = amount;
		this.stock = stock;
		this.seller = seller;
		this.productStatus = IN_STOCK;
		this.orderCount = 0L;
		this.avgScore = (double)0;
	}

	public void decreaseStock(Integer quantity) {
		this.stock -= quantity;
		if (this.stock == 0) {
			productStatus = OUT_OF_STOCK;
		}
	}

	public boolean isSoldOut() {
		return this.productStatus == OUT_OF_STOCK;
	}

	public void increaseOrderCount() {
		this.orderCount++;
	}

	public void updateAvgScore(Double avgScore) {
		this.avgScore = avgScore;
	}
}
