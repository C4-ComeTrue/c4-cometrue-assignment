package org.c4marathon.assignment.global.scheduler;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.c4marathon.assignment.domain.coupon.repository.FailedCouponLogRepository;
import org.c4marathon.assignment.domain.coupon.service.CouponReadService;
import org.c4marathon.assignment.domain.issuedcoupon.repository.IssuedCouponRepository;
import org.c4marathon.assignment.domain.issuedcoupon.service.CouponRestrictionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CouponWithdrawScheduler {

	private final CouponRestrictionManager couponRestrictionManager;
	private final FailedCouponLogRepository failedCouponLogRepository;
	private final IssuedCouponRepository issuedCouponRepository;
	private final CouponReadService couponReadService;

	// failed_coupon_log에 coupon_id가 없고, used_map에 있는 것 중에서, 쿠폰 유효 기간이 지난 것을 삭제해야함
	// failed_coupon_log에 채워지기 전에 이 스케줄러가 돌아가지고 existsByCouponId가 false를 리턴하면 어떡하지..?
	@Scheduled(fixedRate = 60 * 1_000)
	@Transactional
	public void processCouponWithdraw() {
		for (Long usedCouponId : couponRestrictionManager.getNotUsableCoupons()) {
			Coupon coupon = couponReadService.findById(usedCouponId);
			if (coupon.getExpiredTime().isBefore(LocalDateTime.now())) {
				if (!failedCouponLogRepository.existsByCouponId(usedCouponId)) {
					issuedCouponRepository.deleteByCouponId(usedCouponId);
					couponRestrictionManager.removeNotUsableCoupon(usedCouponId);
				}
			}
		}
	}
}
