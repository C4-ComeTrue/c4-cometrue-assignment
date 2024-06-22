package org.c4marathon.assignment.domain.issuedcoupon.service;

import static org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCouponFactory.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.c4marathon.assignment.domain.coupon.service.CouponReadService;
import org.c4marathon.assignment.domain.issuedcoupon.dto.request.CouponIssueRequest;
import org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCoupon;
import org.c4marathon.assignment.domain.issuedcoupon.repository.IssuedCouponRepository;
import org.c4marathon.assignment.global.aop.annotation.CouponIssueLock;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockedCouponService {

	private final CouponReadService couponReadService;
	private final IssuedCouponReadService issuedCouponReadService;
	private final CouponRestrictionManager couponRestrictionManager;
	private final IssuedCouponRepository issuedCouponRepository;

	@CouponIssueLock(key = "#eventId")
	public long increaseIssueCount(Long eventId, CouponIssueRequest request, Consumer consumer) {
		validateRedundantIssue(eventId, consumer.getId(), request.couponId());
		Coupon coupon = couponReadService.findById(request.couponId());
		coupon.increaseIssuedCount();
		return issuedCouponRepository.save(buildIssuedCoupon(request, consumer)).getId();
	}

	@CouponIssueLock(key = "#couponId")
	public void increaseUsedCount(Long couponId, Long issuedCouponId) {
		Coupon coupon = couponReadService.findById(couponId);
		IssuedCoupon issuedCoupon = issuedCouponReadService.findById(issuedCouponId);
		coupon.increaseUsedCount();
		issuedCoupon.increaseUsedCount();
	}

	@CouponIssueLock(key = "#couponId")
	public void decreaseUsedCount(Long couponId, Long issuedCouponId) {
		Coupon coupon = couponReadService.findById(couponId);
		IssuedCoupon issuedCoupon = issuedCouponReadService.findById(issuedCouponId);
		coupon.decreaseUsedCount();
		issuedCoupon.decreaseUsedCount();
	}

	private void validateRedundantIssue(Long eventId, Long consumerId, Long couponId) {
		if (issuedCouponReadService.existsByConsumerIdAndEventId(consumerId, eventId)) {
			couponRestrictionManager.addNotIssuableCoupon(couponId);
			throw SINGLE_COUPON_AVAILABLE_PER_EVENT.baseException();
		}
	}
}
