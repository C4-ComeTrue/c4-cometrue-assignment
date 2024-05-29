package org.c4marathon.assignment.domain.product.dto.response;

import java.time.LocalDateTime;

public record ProductSearchEntry(
	long id,
	String name,
	String description,
	long amount,
	int stock,
	LocalDateTime createdAt,
	Long orderCount,
	Double avgScore
) {
}
