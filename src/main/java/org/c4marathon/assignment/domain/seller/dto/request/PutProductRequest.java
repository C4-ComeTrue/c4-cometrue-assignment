package org.c4marathon.assignment.domain.seller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PutProductRequest(
	@Size(max = 100, message = "name length exceed 100")
	@NotNull(message = "name is null")
	String name,
	@Size(max = 500, message = "description length exceed 500")
	@NotNull(message = "description is null")
	String description,
	long amount,
	int stock
) {
}
