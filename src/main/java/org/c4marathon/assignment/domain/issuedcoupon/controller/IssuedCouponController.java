package org.c4marathon.assignment.domain.issuedcoupon.controller;

import org.c4marathon.assignment.domain.issuedcoupon.dto.request.CouponIssueRequest;
import org.c4marathon.assignment.domain.issuedcoupon.service.IssuedCouponService;
import org.c4marathon.assignment.global.auth.ConsumerThreadLocal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/issued-coupons")
public class IssuedCouponController {

	private final IssuedCouponService issuedCouponService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public long issueCoupon(@Valid @RequestBody CouponIssueRequest request) {
		return issuedCouponService.issueCoupon(request, ConsumerThreadLocal.get());
	}
}
