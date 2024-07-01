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

	/**
	 * 선착순 발급 쿠폰에서 발급 시에 issuedCount를 증가시키는 로직
	 * 선착순 발급 쿠폰은 "한 이벤트에서 하나의 쿠폰 종류만 발급" 받을 수 있음.
	 * 그래서 만약 maximumUsage == usedCount 이면, 다음 요청이 굳이 레디스 락 걸고 이 메서드를 실행시키지 않아도 되므로
	 * couponRestrictionManager에 캐싱함
	 */
	@CouponIssueLock(key = "#eventId")
	public long increaseIssueCount(Long eventId, CouponIssueRequest request, Consumer consumer) {
		validateRedundantIssue(eventId, consumer.getId());
		Coupon coupon = couponReadService.findById(request.couponId());
		if (coupon.getIssuedCount().equals(coupon.getMaximumIssued())) {
			couponRestrictionManager.addNotIssuableCoupon(coupon.getId());
		}
		coupon.increaseIssuedCount();
		return issuedCouponRepository.save(buildIssuedCoupon(request, consumer)).getId();
	}

	/**
	 * 선착순 사용 쿠폰에서 사용 시에 usedCount를 증가시키는 로직
	 * 이것도 마찬가지로 couponRestrictionManager에 캐싱함
	 */
	@CouponIssueLock(key = "#couponId")
	public void increaseUsedCount(Long couponId, Long issuedCouponId) {
		Coupon coupon = couponReadService.findById(couponId);
		IssuedCoupon issuedCoupon = issuedCouponReadService.findById(issuedCouponId);
		if (coupon.getUsedCount().equals(coupon.getMaximumUsage())) {
			couponRestrictionManager.addNotUsableCoupon(coupon.getId());
		}
		coupon.increaseUsedCount();
		issuedCoupon.increaseUsedCount();
	}

	/**
	 * 쿠폰 사용 로직 이후, 예외가 발생했을 때 쿠폰 사용 로직은 다른 트랜잭션에서 진행됐으므로 수행해야 하는 보상 로직
	 * 쿠폰의 usedCount를 감소
	 */
	@CouponIssueLock(key = "#couponId")
	public void decreaseUsedCount(Long couponId, Long issuedCouponId) {
		Coupon coupon = couponReadService.findById(couponId);
		IssuedCoupon issuedCoupon = issuedCouponReadService.findById(issuedCouponId);
		coupon.decreaseUsedCount();
		issuedCoupon.decreaseUsedCount();
	}

	private void validateRedundantIssue(Long eventId, Long consumerId) {
		if (issuedCouponReadService.existsByConsumerIdAndEventId(consumerId, eventId)) {
			throw SINGLE_COUPON_AVAILABLE_PER_EVENT.baseException();
		}
	}
}
