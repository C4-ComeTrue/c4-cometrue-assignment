package org.c4marathon.assignment.domain.coupon.dto.request;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.global.constant.CouponType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCouponRequest(
	@NotEmpty
	@Size(max = 20)
	String name,
	@NotNull
	CouponType couponType,
	boolean redundantUsable,
	long discountPolicyId,
	long eventId,
	@NotNull
	@Future
	LocalDateTime validity,
	Long maximumUsage,
	Long maximumIssued
) {

	/**
	 * 선착순 사용쿠폰인 경우 maximumUsage가 null이면 안됨
	 * 선착순 발급쿠폰인 경우 maximumIssued가 null이면 안됨
	 */
	public void validate() {
		if (couponType == CouponType.USE_COUPON && maximumUsage == null) {
			throw BIND_ERROR.baseException();
		}
		if (couponType == CouponType.ISSUE_COUPON && maximumIssued == null) {
			throw BIND_ERROR.baseException();
		}
	}
}
