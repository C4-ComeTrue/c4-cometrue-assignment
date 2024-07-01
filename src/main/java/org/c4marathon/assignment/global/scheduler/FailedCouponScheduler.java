package org.c4marathon.assignment.global.scheduler;

import java.util.Optional;

import org.c4marathon.assignment.domain.coupon.entity.FailedCouponLog;
import org.c4marathon.assignment.domain.coupon.repository.FailedCouponLogRepository;
import org.c4marathon.assignment.domain.issuedcoupon.service.LockedCouponService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FailedCouponScheduler {

	private final FailedCouponLogRepository failedCouponLogRepository;
	private final LockedCouponService lockedCouponService;

	/**
	 * 쿠폰 사용 시 실패했는데, 재시도 3번 조차 실패한 쿠폰 사용 로직에 대한 처리를 맡는 스케줄러
	 * 5초마다 돌게 되는데, 계속 실패하는 거 때문에 무한 루프 타지 않도록 큐처럼 선입 선출로 처리함.
	 */
	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void scheduleFailedCouponLog() {
		long count = failedCouponLogRepository.count();
		while (count-- > 0) {
			Optional<FailedCouponLog> failedCouponLogOptional = failedCouponLogRepository.findFirst();
			if (failedCouponLogOptional.isEmpty()) {
				break;
			}
			FailedCouponLog failedCouponLog = failedCouponLogOptional.get();
			try {
				lockedCouponService.decreaseUsedCount(failedCouponLog.getIssuedCouponId(),
					failedCouponLog.getCouponId());
			} catch (Exception e) {
				failedCouponLogRepository.save(new FailedCouponLog(null, failedCouponLog.getIssuedCouponId(),
					failedCouponLog.getCouponId()));
			} finally {
				failedCouponLogRepository.delete(failedCouponLog);
			}
		}
	}
}
