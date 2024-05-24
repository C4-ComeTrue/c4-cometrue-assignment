package org.c4marathon.assignment.domain.review.entity;

import org.c4marathon.assignment.domain.review.dto.request.ReviewCreateRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewFactory {

	public static Review buildReview(Long consumerId, ReviewCreateRequest request) {
		return Review.builder()
			.consumerId(consumerId)
			.productId(request.productId())
			.score(request.score())
			.comment(request.comment())
			.build();
	}
}
