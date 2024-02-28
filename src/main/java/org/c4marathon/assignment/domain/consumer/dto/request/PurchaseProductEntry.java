package org.c4marathon.assignment.domain.consumer.dto.request;

import jakarta.validation.constraints.Min;

public record PurchaseProductEntry(
	long productId,
	@Min(value = 1, message = "quantity is less than 1")
	int quantity
) {
}
