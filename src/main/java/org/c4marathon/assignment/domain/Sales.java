package org.c4marathon.assignment.domain;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sales {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sales_pk")
	private Long salesPk;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender")
	private Member sender;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver")
	private Member receiver;

	@Column(name = "charge_type")
	@Enumerated(EnumType.STRING)
	private ChargeType chargeType;

	@Column(name = "value")
	private Integer value;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_item")
	private OrderItem orderItem;

}
