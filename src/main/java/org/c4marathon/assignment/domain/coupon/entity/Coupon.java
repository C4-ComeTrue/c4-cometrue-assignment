package org.c4marathon.assignment.domain.coupon.entity;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;
import org.c4marathon.assignment.global.constant.CouponType;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "coupon_tbl"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Coupon extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "coupon_id", columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "name", columnDefinition = "VARCHAR(20)")
	private String name;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "coupon_type", columnDefinition = "VARCHAR(20)", updatable = false)
	private CouponType couponType;

	@NotNull
	@Column(name = "redundant_usable", columnDefinition = "BIT", updatable = false)
	private Boolean redundantUsable;

	@NotNull
	@Column(name = "discount_policy_id", columnDefinition = "BIGINT")
	private Long discountPolicyId;

	@NotNull
	@Column(name = "event_id", columnDefinition = "BIGINT")
	private Long eventId;

	@NotNull
	@Column(name = "validity", columnDefinition = "DATETIME")
	private LocalDateTime validity;

	@Column(name = "maximum_usage", columnDefinition = "BIGINT default " + Long.MAX_VALUE)
	private Long maximumUsage;

	@Column(name = "maximum_issued", columnDefinition = "BIGINT default " + Long.MAX_VALUE)
	private Long maximumIssued;

	@NotNull
	@Column(name = "use_count", columnDefinition = "BIGINT default 0")
	private Long useCount;

	@NotNull
	@Column(name = "issued_count", columnDefinition = "BIGINT default 0")
	private Long issuedCount;

	@Builder
	public Coupon(
		String name,
		CouponType couponType,
		Boolean redundantUsable,
		Long discountPolicyId,
		Long eventId,
		LocalDateTime validity,
		Long maximumUsage,
		Long maximumIssued
	) {
		this.name = name;
		this.couponType = couponType;
		this.redundantUsable = redundantUsable;
		this.discountPolicyId = discountPolicyId;
		this.eventId = eventId;
		this.validity = validity;
		this.maximumUsage = maximumUsage;
		this.maximumIssued = maximumIssued;
		this.useCount = 0L;
		this.issuedCount = 0L;
	}
}
