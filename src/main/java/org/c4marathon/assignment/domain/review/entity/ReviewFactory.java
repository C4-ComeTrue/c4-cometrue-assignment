package org.c4marathon.assignment.domain.review.entity;

import org.c4marathon.assignment.domain.review.dto.request.ReviewCreateRequest;
import org.springframework.stereotype.Component;

@Component
public class ReviewFactory {

	public Review buildReview(Long consumerId, ReviewCreateRequest request) {
		return Review.builder()
			.consumerId(consumerId)
			.productId(request.productId())
			.score(request.score())
			.comment(request.comment())
			.build();
	}
}
