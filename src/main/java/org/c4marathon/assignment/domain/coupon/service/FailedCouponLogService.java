package org.c4marathon.assignment.domain.coupon.service;

import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.c4marathon.assignment.domain.coupon.entity.FailedCouponLog;
import org.c4marathon.assignment.domain.coupon.repository.FailedCouponLogRepository;
import org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCoupon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedCouponLogService {

	private final FailedCouponLogRepository failedCouponLogRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveFailedCouponLog(IssuedCoupon issuedCoupon, Coupon coupon) {
		log.info("save failed coupon log");
		failedCouponLogRepository.save(new FailedCouponLog(null, coupon.getId(), issuedCoupon.getId()));
	}
}
