package org.c4marathon.assignment.domain;

import java.util.List;

import org.c4marathon.assignment.exception.ErrorCd;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Item {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long itemPk;

	@ManyToOne
	@NotBlank
	private Member seller;

	@NotBlank
	private String description;

	@NotBlank
	private Integer stock;

	@NotBlank
	private Integer price;

	@OneToMany
	private List<OrderItem> orderItemList;

	@NotBlank
	@ColumnDefault("false")
	@Column(columnDefinition = "TINYINT(1)")
	private boolean isDisplayed; // 판매 여부

	public void addStock(int quantity) {
		this.stock += quantity;
	}

	public void removeStock(int quantity){
		this.stock -= quantity;
	}

}
