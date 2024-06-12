package org.c4marathon.assignment.domain.discountpolicy.service;

import static org.c4marathon.assignment.domain.discountpolicy.entity.DiscountPolicyFactory.*;
import static org.c4marathon.assignment.global.constant.DiscountType.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.discountpolicy.dto.request.DiscountPolicyRequest;
import org.c4marathon.assignment.domain.discountpolicy.repository.DiscountPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DiscountPolicyService {

	private final DiscountPolicyRepository discountPolicyRepository;
	private final DiscountPolicyReadService discountPolicyReadService;

	@Transactional
	public void createDiscountPolicy(DiscountPolicyRequest request) {
		validateDiscountPolicy(request);
		if (discountPolicyReadService.existsByName(request.name())) {
			throw ALREADY_DISCOUNT_POLICY_EXISTS.baseException();
		}
		discountPolicyRepository.save(buildDiscountPolicy(request));
	}

	private void validateDiscountPolicy(DiscountPolicyRequest request) {
		if (request.discountType() == FIXED_DISCOUNT && request.discountAmount() == null) {
			throw BIND_ERROR.baseException();
		}
		if (request.discountType() == RATED_DISCOUNT && request.discountRate() == null) {
			throw BIND_ERROR.baseException();
		}
	}
}
