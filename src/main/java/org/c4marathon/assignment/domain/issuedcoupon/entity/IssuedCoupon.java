package org.c4marathon.assignment.domain.issuedcoupon.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "issued_coupon_tbl"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class IssuedCoupon extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "issued_coupon_id", columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "coupon_id", columnDefinition = "BIGINT")
	private Long couponId;

	@NotNull
	@Column(name = "is_used", columnDefinition = "BIT default 0")
	private Boolean isUsed;

	@NotNull
	@Column(name = "consumer_id", columnDefinition = "BIGINT")
	private Long consumerId;
}
