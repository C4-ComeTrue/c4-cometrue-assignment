package org.c4marathon.assignment.domain.discountpolicy.controller;

import org.c4marathon.assignment.domain.discountpolicy.dto.request.DiscountPolicyRequest;
import org.c4marathon.assignment.domain.discountpolicy.service.DiscountPolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discount-policy")
public class DiscountPolicyController {

	private final DiscountPolicyService discountPolicyService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public void createDiscountPolicy(@Valid @RequestBody DiscountPolicyRequest request) {
		discountPolicyService.createDiscountPolicy(request);
	}
}
