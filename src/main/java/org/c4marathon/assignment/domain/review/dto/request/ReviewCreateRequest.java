package org.c4marathon.assignment.domain.review.dto.request;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
	@Range(min = 1, max = 5, message = "invalid score range")
	int score,
	@Size(max = 100, message = "comment length more than 100")
	String comment,
	long productId
) {
}
