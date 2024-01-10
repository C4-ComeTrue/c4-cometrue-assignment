package org.c4marathon.assignment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sales {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long salesPk;

	@ManyToOne
	private Member customer;

	@ManyToOne
	private Member seller;

	@Enumerated(EnumType.STRING)
	private ChargeType chargeType;

	private Integer value;

	@OneToOne
	private OrderItem orderItem;

	public Sales(Member customer, Member seller, ChargeType chargeType, Integer value, OrderItem orderItem) {
		this.customer = customer;
		this.seller = seller;
		this.chargeType = chargeType;
		this.value = value;
		this.orderItem = orderItem;
	}
}
