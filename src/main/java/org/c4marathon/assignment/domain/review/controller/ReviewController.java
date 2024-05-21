package org.c4marathon.assignment.domain.review.controller;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.review.dto.request.ReviewCreateRequest;
import org.c4marathon.assignment.domain.review.service.ReviewService;
import org.c4marathon.assignment.global.auth.ConsumerThreadLocal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	public void createReview(@Valid @RequestBody ReviewCreateRequest request) {
		Consumer consumer = ConsumerThreadLocal.get();
		reviewService.createReview(consumer, request);
	}
}
