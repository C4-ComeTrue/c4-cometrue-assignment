package org.c4marathon.assignment.domain.discountpolicy.dto.request;

import org.c4marathon.assignment.global.constant.DiscountType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DiscountPolicyRequest(
	@NotEmpty
	@Size(max = 20)
	String name,
	@NotNull
	DiscountType discountType,
	Long discountAmount,
	@Positive
	@Max(100)
	Integer discountRate
) {
}
