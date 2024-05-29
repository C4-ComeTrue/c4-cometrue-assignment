package org.c4marathon.assignment.domain.service.review;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;

import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.review.dto.request.ReviewCreateRequest;
import org.c4marathon.assignment.domain.review.service.ReviewService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ReviewServiceTest extends ServiceTestSupport {

	@InjectMocks
	private ReviewService reviewService;
	@Mock
	private ProductReadService productReadService;
	@Mock
	private OrderProductReadService orderProductReadService;

	@DisplayName("리뷰 작성 시")
	@Nested
	class CreateReview {

		@DisplayName("처음 리뷰를 작성하는 것이라면, review entity가 생성되고 product의 avgScore가 수정된다.")
		@Test
		void updateAvgScore_when_createReview() {
			ReviewCreateRequest request = new ReviewCreateRequest(3, "comment", 1);

			given(productReadService.findById(anyLong())).willReturn(product);
			given(product.getAvgScore()).willReturn(BigDecimal.ZERO);
			given(reviewRepository.countByProductId(anyLong())).willReturn(0L);
			given(orderProductReadService.existsByConsumerIdAndProductId(anyLong(), anyLong())).willReturn(true);

			reviewService.createReview(consumer, request);

			then(product)
				.should(times(1))
				.updateAvgScore(any(BigDecimal.class));
			then(reviewRepository)
				.should(times(1))
				.save(any());
		}

		@DisplayName("중복 리뷰는 불가능하다.")
		@Test
		void fail_when_duplicateReview() {
			ReviewCreateRequest request = new ReviewCreateRequest(3, "comment", 1);

			given(orderProductReadService.existsByConsumerIdAndProductId(anyLong(), anyLong())).willReturn(true);
			given(reviewRepository.existsByConsumerIdAndProductId(anyLong(), anyLong())).willReturn(true);

			ErrorCode errorCode = ErrorCode.REVIEW_ALREADY_EXISTS;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> reviewService.createReview(consumer, request))
				.isInstanceOf(exception.getClass())
				.hasMessageMatching(exception.getMessage());
		}

		@DisplayName("구매 이력이 존재하지 않으면, 리뷰는 불가능하다.")
		@Test
		void fail_when_notExistsOrderProduct() {
			ReviewCreateRequest request = new ReviewCreateRequest(3, "comment", 1);

			given(orderProductReadService.existsByConsumerIdAndProductId(anyLong(), anyLong())).willReturn(false);

			ErrorCode errorCode = ErrorCode.NOT_POSSIBLE_CREATE_REVIEW;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> reviewService.createReview(consumer, request))
				.isInstanceOf(exception.getClass())
				.hasMessageMatching(exception.getMessage());
		}
	}
}
