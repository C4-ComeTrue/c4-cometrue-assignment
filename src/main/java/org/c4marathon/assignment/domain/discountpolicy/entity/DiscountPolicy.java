package org.c4marathon.assignment.domain.discountpolicy.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;
import org.c4marathon.assignment.global.constant.DiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "discount_policy_tbl"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class DiscountPolicy extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "discount_policy_id", columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "name", columnDefinition = "VARCHAR(20)", unique = true)
	private String name;

	@Enumerated(value = EnumType.STRING)
	@NotNull
	@Column(name = "discount_type", columnDefinition = "VARCHAR(20)")
	private DiscountType discountType;

	@Column(name = "discount_amount", columnDefinition = "BIGINT")
	private Long discountAmount;

	@Column(name = "discount_rate", columnDefinition = "INTEGER")
	private Integer discountRate;
}
