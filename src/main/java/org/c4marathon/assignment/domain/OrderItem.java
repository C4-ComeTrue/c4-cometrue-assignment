package org.c4marathon.assignment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderItemPk;

	@ManyToOne
	private Item item;

	@ManyToOne
	private Order order;

	private int price;

	private int count;

	public int getTotalPrice() {
		return this.price * this.count;
	}

}
