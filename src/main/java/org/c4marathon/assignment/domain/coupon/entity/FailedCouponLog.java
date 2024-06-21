package org.c4marathon.assignment.domain.coupon.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	name = "failed_coupon_log_tbl"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
public class FailedCouponLog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "failed_coupon_log_id", columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "coupon_id", columnDefinition = "BIGINT")
	private Long couponId;

	@NotNull
	@Column(name = "issued_coupon_id", columnDefinition = "BIGINT")
	private Long issuedCouponId;
}
