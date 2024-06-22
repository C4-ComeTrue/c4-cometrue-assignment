package org.c4marathon.assignment.domain.coupon.service;

import static org.c4marathon.assignment.global.constant.CouponType.*;

import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.c4marathon.assignment.domain.coupon.entity.FailedCouponLog;
import org.c4marathon.assignment.domain.coupon.repository.FailedCouponLogRepository;
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
	private final FailedCouponLogRepository failedCouponLogRepository;

	/**
	 * lockedCouponService.increaseUsedCount가 실행된 이후에, 어떤 예외가 발생하면 아래 보상 로직이 실행됨.
	 * 보상 로직이기 때문에 무한대로 재시도 할 수는 없고..
	 * 최대 3번 재시도 이후에 그래도 실패하면 FailedCouponLog로 남기게됨.
	 */
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
					failedCouponLogRepository.save(new FailedCouponLog(null, coupon.getId(), issuedCoupon.getId()));
					throw innerException;
				}
			}
		}
	}
}
