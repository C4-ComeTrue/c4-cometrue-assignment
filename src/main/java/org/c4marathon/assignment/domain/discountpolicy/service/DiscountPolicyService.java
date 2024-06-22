package org.c4marathon.assignment.domain.discountpolicy.service;

import static org.c4marathon.assignment.domain.discountpolicy.entity.DiscountPolicyFactory.*;
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
		request.validate();
		if (discountPolicyReadService.existsByName(request.name())) {
			throw ALREADY_DISCOUNT_POLICY_EXISTS.baseException();
		}
		discountPolicyRepository.save(buildDiscountPolicy(request));
	}
}
