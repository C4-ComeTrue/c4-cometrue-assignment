package org.c4marathon.assignment.domain.coupon.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.coupon.dto.request.CreateCouponRequest;
import org.c4marathon.assignment.domain.coupon.entity.CouponFactory;
import org.c4marathon.assignment.domain.coupon.repository.CouponRepository;
import org.c4marathon.assignment.domain.discountpolicy.service.DiscountPolicyReadService;
import org.c4marathon.assignment.domain.event.entity.Event;
import org.c4marathon.assignment.domain.event.service.EventReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

	private final CouponRepository couponRepository;
	private final DiscountPolicyReadService discountPolicyReadService;
	private final EventReadService eventReadService;
	private final CouponReadService couponReadService;

	@Transactional
	public void createCoupon(CreateCouponRequest request) {
		validateRequest(request);
		couponRepository.save(CouponFactory.buildCoupon(request));
	}

	private void validateRequest(CreateCouponRequest request) {
		request.validate();
		if (couponReadService.existsByName(request.name())) {
			throw ALREADY_COUPON_EXISTS.baseException();
		}
		if (!discountPolicyReadService.existsById(request.discountPolicyId())) {
			throw DISCOUNT_POLICY_NOT_FOUND.baseException();
		}
		Event event = eventReadService.findById(request.eventId());
		if (event.getEndDate().isBefore(request.expiredTime())) {
			throw INVALID_COUPON_EXPIRED_TIME.baseException();
		}
	}
}
