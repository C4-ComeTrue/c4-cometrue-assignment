package org.c4marathon.assignment.domain.discountpolicy.dto.request;

import static org.c4marathon.assignment.global.constant.DiscountType.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

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

	public void validate() {
		if (discountType == FIXED_DISCOUNT && discountAmount == null) {
			throw BIND_ERROR.baseException();
		}
		if (discountType == RATED_DISCOUNT && discountRate == null) {
			throw BIND_ERROR.baseException();
		}
	}
}
