package org.c4marathon.assignment.domain.service.review;

import static org.assertj.core.api.Assertions.*;

import org.c4marathon.assignment.domain.review.dto.request.ReviewCreateRequest;
import org.c4marathon.assignment.domain.review.entity.Review;
import org.c4marathon.assignment.domain.review.entity.ReviewFactory;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

public class ReviewFactoryTest extends ServiceTestSupport {

	@InjectMocks
	private ReviewFactory reviewFactory;

	@DisplayName("review entity 생성 시")
	@Nested
	class BuildReview {

		@Test
		@DisplayName("review entity 생성 시 request에 맞는 필드가 주입된다.")
		void createReviewEntity() {
			Review review = reviewFactory.buildReview(1L, new ReviewCreateRequest(3, "comment", 1));
			assertThat(review.getProductId()).isEqualTo(1L);
			assertThat(review.getComment()).isEqualTo("comment");
			assertThat(review.getScore()).isEqualTo(3);
		}
	}
}
