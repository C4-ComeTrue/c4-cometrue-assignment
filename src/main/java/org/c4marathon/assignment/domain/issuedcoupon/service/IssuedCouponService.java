package org.c4marathon.assignment.domain.issuedcoupon.service;

import static org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCouponFactory.*;
import static org.c4marathon.assignment.global.constant.CouponType.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.c4marathon.assignment.domain.coupon.service.CouponReadService;
import org.c4marathon.assignment.domain.event.entity.Event;
import org.c4marathon.assignment.domain.event.service.EventReadService;
import org.c4marathon.assignment.domain.issuedcoupon.dto.request.CouponIssueRequest;
import org.c4marathon.assignment.domain.issuedcoupon.repository.IssuedCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssuedCouponService {

	private final IssuedCouponRepository issuedCouponRepository;
	private final IssuedCouponReadService issuedCouponReadService;
	private final CouponReadService couponReadService;
	private final EventReadService eventReadService;
	private final LockedCouponService lockedCouponService;
	private final CouponRestrictionManager couponRestrictionManager;

	/**
	 * 쿠폰 발급
	 * 하나의 이벤트에 여러개의 쿠폰이 생성될 수 있는데, 소비자는 하나의 이벤트에 하나의 쿠폰만 발급받을 수 있음
	 * 선착순 발급 쿠폰일 때, 캐싱된 "이미 발급된 쿠폰 수"가 최대 발급 가능 수 이상이면(높을리는 없겠지만) 예외 터트림
	 * 이걸로 일단 레디스 접근을 최소화하고..
	 * 그게 아니라면 락 얻고 확인한 다음에 가능하면 쿠폰 발급함.
	 * 선착순 발급 쿠폰은 이벤트 하나 당 한 개의 쿠폰만 발급받을 수 있음
	 * 선착순 사용 쿠폰은 이벤트 하나 당 여러개의 쿠폰을 발급받을 수 있음
	 */
	@Transactional
	public long issueCoupon(CouponIssueRequest request, Consumer consumer) {
		Coupon coupon = couponReadService.findById(request.couponId());
		Event event = eventReadService.findById(coupon.getEventId());
		validateExpiration(coupon.getExpiredTime());

		if (coupon.getCouponType() == ISSUE_COUPON) {
			couponRestrictionManager.validateCouponIssuable(coupon.getId());
			return lockedCouponService.increaseIssueCount(event.getId(), request, consumer);
		}
		return issuedCouponRepository.save(buildIssuedCoupon(request, consumer)).getId();
	}

	private void validateExpiration(LocalDateTime target) {
		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(target)) {
			throw RESOURCE_EXPIRED.baseException();
		}
	}
}
