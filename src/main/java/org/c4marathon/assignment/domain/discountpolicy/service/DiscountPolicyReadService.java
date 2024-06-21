package org.c4marathon.assignment.domain.discountpolicy.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.discountpolicy.entity.DiscountPolicy;
import org.c4marathon.assignment.domain.discountpolicy.repository.DiscountPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DiscountPolicyReadService {

	private final DiscountPolicyRepository discountPolicyRepository;

	@Transactional(readOnly = true)
	public boolean existsByName(String name) {
		return discountPolicyRepository.existsByName(name);
	}

	@Transactional(readOnly = true)
	public boolean existsById(Long id) {
		return discountPolicyRepository.existsById(id);
	}

	@Transactional(readOnly = true)
	public DiscountPolicy findById(Long id) {
		return discountPolicyRepository.findById(id)
			.orElseThrow(DISCOUNT_POLICY_NOT_FOUND::baseException);
	}
}
