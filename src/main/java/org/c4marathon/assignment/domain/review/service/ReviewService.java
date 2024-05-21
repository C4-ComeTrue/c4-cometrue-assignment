package org.c4marathon.assignment.domain.review.service;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.review.dto.request.ReviewCreateRequest;
import org.c4marathon.assignment.domain.review.entity.ReviewFactory;
import org.c4marathon.assignment.domain.review.repository.ReviewRepository;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ReviewFactory reviewFactory;
	private final ProductReadService productReadService;

	/**
	 * score, comment를 입력받아 Review entity 생성
	 * product avgScore update
	 * @throws org.c4marathon.assignment.global.error.BaseException 이미 해당 product에 대한 리뷰를 작성한 경우
	 */
	@Transactional
	public void createReview(Consumer consumer, ReviewCreateRequest request) {
		if (reviewRepository.existsByConsumerIdAndProductId(consumer.getId(), request.productId())) {
			throw ErrorCode.REVIEW_ALREADY_EXISTS.baseException();
		}

		Product product = productReadService.findById(request.productId());
		Long reviewCount = productReadService.findReviewCount(request.productId());
		product.updateAvgScore(calculateAvgScore(product.getAvgScore(), reviewCount, request.score()));
		reviewRepository.save(reviewFactory.buildReview(consumer.getId(), request));
	}

	private Double calculateAvgScore(Double avgScore, Long reviewCount, int score) {
		return ((avgScore * reviewCount) + score) / (reviewCount + 1);
	}
}
