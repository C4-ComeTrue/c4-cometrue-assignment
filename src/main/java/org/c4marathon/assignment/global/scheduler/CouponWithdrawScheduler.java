package org.c4marathon.assignment.global.scheduler;

import java.time.LocalDateTime;
import java.util.List;

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
	private final IssuedCouponRepository issuedCouponRepository;

	/**
	 * 쿠폰 유효기간이 지난 것을 삭제
	 * 유효기간이 지난 쿠폰을 확인하면서, couponRestrictionManager에 캐싱된 데이터도 함께 삭제
	 */
	@Scheduled(fixedDelay = 60 * 1_000)
	@Transactional
	public void scheduleCouponWithdraw() {
		List<Long> expiredCouponIds = issuedCouponRepository.findExpiredCouponId(LocalDateTime.now());
		for (Long expiredCouponId : expiredCouponIds) {
			couponRestrictionManager.removeNotUsableCoupon(expiredCouponId);
			issuedCouponRepository.deleteByCouponId(expiredCouponId);
		}
	}
}
