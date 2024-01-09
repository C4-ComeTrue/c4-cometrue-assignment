package org.c4marathon.assignment.domain.consumer.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchaseProductEntry(
	@NotNull(message = "productId is null")
	Long productId,
	@NotNull(message = "quantity is null")
	@Min(value = 1, message = "quantity is less than 1")
	Integer quantity
) {
}
