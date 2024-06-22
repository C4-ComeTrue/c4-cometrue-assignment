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

	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void processFailedCouponLog() {
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
