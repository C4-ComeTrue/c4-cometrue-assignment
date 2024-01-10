package org.c4marathon.assignment.domain;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@ColumnDefault("false")
	@Column(columnDefinition = "TINYINT(1)")
	private boolean isProceeded; // 이 주문 내역의 승인여부를 표시합니다.

	public int getTotalPrice(){
		return this.price * this.count;
	}

}
