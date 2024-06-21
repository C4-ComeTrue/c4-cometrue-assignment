package org.c4marathon.assignment.domain.coupon.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.c4marathon.assignment.domain.coupon.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponReadService {

	private final CouponRepository couponRepository;

	@Transactional(readOnly = true)
	public boolean existsByName(String name) {
		return couponRepository.existsByName(name);
	}

	@Transactional(readOnly = true)
	public Coupon findById(Long id) {
		return couponRepository.findById(id)
			.orElseThrow(COUPON_NOT_FOUND::baseException);
	}
}
