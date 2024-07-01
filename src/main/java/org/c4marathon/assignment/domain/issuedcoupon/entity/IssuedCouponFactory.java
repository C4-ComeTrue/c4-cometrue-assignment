package org.c4marathon.assignment.domain.issuedcoupon.entity;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.issuedcoupon.dto.request.CouponIssueRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IssuedCouponFactory {

	public static IssuedCoupon buildIssuedCoupon(CouponIssueRequest request, Consumer consumer) {
		return IssuedCoupon.builder()
			.couponId(request.couponId())
			.consumerId(consumer.getId())
			.build();
	}
}
