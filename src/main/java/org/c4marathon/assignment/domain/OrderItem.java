package org.c4marathon.assignment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ord_item_pk")
	private Long orderItemPk;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item")
	private Item item;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ord")
	private Order order;

	@Column(name = "price")
	private int price;

	@Column(name = "count")
	private int count;

	@Column(name = "total_price")
	private int totalPrice;

	public int generateTotalPrice() {
		return this.price * this.count;
	}

}
