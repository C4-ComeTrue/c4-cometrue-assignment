package org.c4marathon.assignment.domain.product.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;
import org.c4marathon.assignment.domain.seller.entity.Seller;

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
@Table(name = "product_tbl")
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
	}
}
