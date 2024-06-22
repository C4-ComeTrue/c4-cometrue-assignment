package org.c4marathon.assignment.domain.issuedcoupon.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class CouponRestrictionManager {

	// 쿠폰의 수가 수십만개가 될 순 없고, 스케줄러로 돌면서 기간이 지난 쿠폰 또는 이벤트인 경우
	// 삭제할 것이기 때문에 캐시 라이브러리를 안쓰고 그냥 Map으로 함
	private final Set<Long> notIssuableCoupons = new HashSet<>();
	private final Set<Long> notUsableCoupons = new HashSet<>();

	public void validateCouponIssuable(long couponId) {
		if (notIssuableCoupons.contains(couponId)) {
			throw COUPON_NOT_ISSUABLE.baseException();
		}
	}

	public void addNotIssuableCoupon(long couponId) {
		notIssuableCoupons.add(couponId);
	}

	public void validateCouponUsable(long couponId) {
		if (notUsableCoupons.contains(couponId)) {
			throw COUPON_NOT_USABLE.baseException();
		}
	}

	public void addNotUsableCoupon(long couponId) {
		notUsableCoupons.add(couponId);
	}

	public void removeNotUsableCoupon(long couponId) {
		notUsableCoupons.remove(couponId);
	}
}
