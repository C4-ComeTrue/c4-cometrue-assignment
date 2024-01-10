package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ORD")
@Getter
@Setter
@NoArgsConstructor
public class Order {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderPk;

	@ManyToOne
	private Member customer;

	@OneToMany
	private List<OrderItem> orderItems = new ArrayList<>();

	@OneToOne
	private Payment payment;

	private LocalDateTime orderDate;

	@OneToOne
	private Shipment shipment;

}
