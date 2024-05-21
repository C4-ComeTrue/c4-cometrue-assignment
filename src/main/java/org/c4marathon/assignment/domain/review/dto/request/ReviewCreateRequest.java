package org.c4marathon.assignment.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
	@Min(value = 1, message = "score less than 1")
	@Max(value = 5, message = "score more than 5")
	int score,
	@Size(max = 100, message = "comment length more than 100")
	String comment,
	long productId
) {
}
