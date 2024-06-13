package org.c4marathon.assignment.domain.coupon.entity;

import org.c4marathon.assignment.domain.coupon.dto.request.CreateCouponRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponFactory {

	public static Coupon buildCoupon(CreateCouponRequest request) {
		return Coupon.builder()
			.name(request.name())
			.couponType(request.couponType())
			.redundantUsable(request.redundantUsable())
			.discountPolicyId(request.discountPolicyId())
			.eventId(request.eventId())
			.validity(request.validity())
			.maximumUsage(request.maximumUsage())
			.maximumIssued(request.maximumIssued())
			.build();
	}
}
