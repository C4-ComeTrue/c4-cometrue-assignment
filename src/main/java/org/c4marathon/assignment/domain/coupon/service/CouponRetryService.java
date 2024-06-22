package org.c4marathon.assignment.domain.coupon.service;

import static org.c4marathon.assignment.global.constant.CouponType.*;

import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCoupon;
import org.c4marathon.assignment.domain.issuedcoupon.service.CouponRestrictionManager;
import org.c4marathon.assignment.domain.issuedcoupon.service.LockedCouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponRetryService {

	public static final int MAXIMUM_RETRY_COUNT = 3;
	private final LockedCouponService lockedCouponService;
	private final CouponRestrictionManager couponRestrictionManager;
	private final FailedCouponLogService failedCouponLogService;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void decreaseUsedCount(IssuedCoupon issuedCoupon, Coupon coupon) {
		if (issuedCoupon == null || coupon == null || coupon.getCouponType() != USE_COUPON) {
			return;
		}
		int retryCount = 0;
		while (true) {
			try {
				lockedCouponService.decreaseUsedCount(coupon.getId(), issuedCoupon.getId());
				couponRestrictionManager.removeNotUsableCoupon(coupon.getId());
				break;
			} catch (Exception innerException) {
				log.info("failed decrease coupon use count. retry count: {}", retryCount);
				retryCount++;
				if (retryCount >= MAXIMUM_RETRY_COUNT) {
					failedCouponLogService.saveFailedCouponLog(issuedCoupon, coupon);
					throw innerException;
				}
			}
		}
	}
}
