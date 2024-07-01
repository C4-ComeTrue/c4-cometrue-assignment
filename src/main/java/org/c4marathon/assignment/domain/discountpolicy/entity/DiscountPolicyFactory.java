package org.c4marathon.assignment.domain.discountpolicy.entity;

import org.c4marathon.assignment.domain.discountpolicy.dto.request.DiscountPolicyRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscountPolicyFactory {

	public static DiscountPolicy buildDiscountPolicy(DiscountPolicyRequest request) {
		return new DiscountPolicy(null, request.name(), request.discountType(), request.discountAmount(),
			request.discountRate());
	}
}
