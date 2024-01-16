package org.c4marathon.assignment.domain;

import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long itemPk;

	@ManyToOne
	@NotNull
	private Member seller;

	@NotBlank
	private String name;

	@NotBlank
	private String description;

	@NotNull
	private Integer stock;

	@NotNull
	private Integer price;

	@OneToMany
	private List<OrderItem> orderItemList;

	@NotNull
	@ColumnDefault("false")
	@Column(columnDefinition = "TINYINT(1)")
	private boolean isDisplayed; // 판매 여부

	public void addStock(int quantity) {
		this.stock += quantity;
	}

	public void removeStock(int quantity) {
		this.stock -= quantity;
	}

}
