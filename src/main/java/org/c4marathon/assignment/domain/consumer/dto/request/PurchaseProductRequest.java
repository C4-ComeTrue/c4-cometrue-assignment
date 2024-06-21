package org.c4marathon.assignment.domain.consumer.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

public record PurchaseProductRequest(
	List<@Valid PurchaseProductEntry> purchaseProducts,
	@PositiveOrZero(message = "point less than 0")
	long point,
	Long issuedCouponId
) {
}
