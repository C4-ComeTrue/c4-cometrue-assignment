package org.c4marathon.assignment.domain;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Item {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long itemPk;

	@ManyToOne
	private Member seller;

	private String description;

	private Integer stock;

	private Integer cost;

	@ColumnDefault("false")
	@Column(columnDefinition = "TINYINT(1)")
	private boolean isDisplayed; // 판매 여부

}
