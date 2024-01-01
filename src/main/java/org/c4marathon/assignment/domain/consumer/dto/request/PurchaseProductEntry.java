package org.c4marathon.assignment.domain.consumer.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PurchaseProductEntry {

	@NotNull(message = "productId is null")
	private Long productId;

	@NotNull(message = "quantity is null")
	@Min(value = 1, message = "quantity is less than 1")
	private Integer quantity;
}
