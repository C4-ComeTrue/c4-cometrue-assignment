package org.c4marathon.assignment.domain.coupon.service;

import org.c4marathon.assignment.domain.coupon.repository.CouponRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponReadService {

	private final CouponRepository couponRepository;

	public boolean existsByName(String name) {
		return couponRepository.existsByName(name);
	}
}
