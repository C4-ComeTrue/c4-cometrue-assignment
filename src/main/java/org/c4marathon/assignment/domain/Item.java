package org.c4marathon.assignment.domain;

import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "item_pk")
	private Long itemPk;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller")
	@NotNull
	private Member seller;

	@Column(name = "name")
	@NotBlank
	private String name;

	@Column(name = "description")
	@NotBlank
	private String description;

	@Column(name = "stock")
	@NotNull
	private Integer stock;

	@Column(name = "price")
	@NotNull
	private Integer price;

	@OneToMany(mappedBy = "item")
	private List<OrderItem> orderItemList;

	@NotNull
	@ColumnDefault("false")
	@Column(name = "is_displayed", columnDefinition = "TINYINT(1)")
	private boolean isDisplayed; // 판매 여부

	public void addStock(int quantity) {
		this.stock += quantity;
	}

	public void removeStock(int quantity) {
		this.stock -= quantity;
	}

}
