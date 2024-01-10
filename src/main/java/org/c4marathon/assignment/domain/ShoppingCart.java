package org.c4marathon.assignment.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ShoppingCart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long shoppingCartId;

	@OneToMany
	private List<OrderItem> itemList;

	@OneToOne
	private Member member;

	public ShoppingCart(Member member) {
		this.itemList = new ArrayList<>();
		this.member = member;
	}

	public void addItemToCart(OrderItem item) {
		this.itemList.add(item);
	}
}
