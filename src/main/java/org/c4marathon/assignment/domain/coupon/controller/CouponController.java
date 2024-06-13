package org.c4marathon.assignment.domain.coupon.controller;

import org.c4marathon.assignment.domain.coupon.dto.request.CreateCouponRequest;
import org.c4marathon.assignment.domain.coupon.service.CouponService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

	private final CouponService couponService;

	@PostMapping
	public void createCoupon(@Valid @RequestBody CreateCouponRequest request) {
		couponService.createCoupon(request);
	}
}
