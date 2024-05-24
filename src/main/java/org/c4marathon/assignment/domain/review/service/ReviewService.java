package org.c4marathon.assignment.domain.review.service;

import static org.c4marathon.assignment.domain.review.entity.ReviewFactory.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.review.dto.request.ReviewCreateRequest;
import org.c4marathon.assignment.domain.review.repository.ReviewRepository;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ProductReadService productReadService;
	private final OrderProductReadService orderProductReadService;

	/**
	 * score, comment를 입력받아 Review entity 생성
	 * product avgScore update
	 * @throws org.c4marathon.assignment.global.error.BaseException
	 * productId에 해당하는 구매 이력이 존재하지 않는 경우, 또는 리뷰 작성 가능 기간(30일)이 지난 경우
	 * -> 구매 이력은 구매 이후 30일이 지난 이후에 삭제되기 때문에 구매 이력이 없다면 아예 구매한 적이 없거나, 리뷰 작성 가능 기간이 지난 것을 의미함
	 * 이미 해당 product에 대한 리뷰를 작성한 경우
	 */
	@Transactional
	public void createReview(Consumer consumer, ReviewCreateRequest request) {
		if (!orderProductReadService.existsByConsumerIdAndProductId(consumer.getId(), request.productId())) {
			throw ErrorCode.NOT_POSSIBLE_CREATE_REVIEW.baseException();
		}
		if (reviewRepository.existsByConsumerIdAndProductId(consumer.getId(), request.productId())) {
			throw ErrorCode.REVIEW_ALREADY_EXISTS.baseException();
		}

		Product product = productReadService.findById(request.productId());
		Long reviewCount = reviewRepository.countByProductId(request.productId());
		product.updateAvgScore(calculateAvgScore(product.getAvgScore(), reviewCount, request.score()));
		reviewRepository.save(buildReview(consumer.getId(), request));
	}

	private BigDecimal calculateAvgScore(BigDecimal avgScore, Long reviewCount, int score) {
		return avgScore.multiply(BigDecimal.valueOf(reviewCount))
			.add(BigDecimal.valueOf(score))
			.divide(BigDecimal.valueOf(reviewCount + 1), 4, RoundingMode.DOWN);
	}
}
