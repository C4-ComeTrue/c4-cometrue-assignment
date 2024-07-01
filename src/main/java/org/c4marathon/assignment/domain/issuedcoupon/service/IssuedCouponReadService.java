package org.c4marathon.assignment.domain.issuedcoupon.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCoupon;
import org.c4marathon.assignment.domain.issuedcoupon.repository.IssuedCouponRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class IssuedCouponReadService {

	private final IssuedCouponRepository issuedCouponRepository;

	public boolean existsByConsumerIdAndEventId(Long consumerId, Long eventId) {
		return issuedCouponRepository.existsByConsumerIdCouponId_EventId(consumerId, eventId) != null;
	}

	public IssuedCoupon findById(Long id) {
		return issuedCouponRepository.findById(id)
			.orElseThrow(ISSUED_COUPON_NOT_FOUND::baseException);
	}
}
