package org.c4marathon.assignment.domain.discountpolicy.service;

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
}
