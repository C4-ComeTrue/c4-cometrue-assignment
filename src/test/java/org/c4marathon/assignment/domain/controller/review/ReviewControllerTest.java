package org.c4marathon.assignment.domain.controller.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.c4marathon.assignment.domain.controller.ControllerTestSupport;
import org.c4marathon.assignment.domain.review.dto.request.ReviewCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

public class ReviewControllerTest extends ControllerTestSupport {

	@DisplayName("리뷰 작성 시")
	@Nested
	class CreateReview {

		private static final String REQUEST_URL = "/reviews";

		@DisplayName("올바른 요청을 하면 성공한다.")
		@Test
		void success_when_validRequest() throws Exception {
			ReviewCreateRequest request = new ReviewCreateRequest(1, "comment", 100);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isOk()
				);
		}

		@DisplayName("score가 올바른 범위가 아니면 실패한다.")
		@ParameterizedTest
		@ValueSource(ints = {0, -1, 6, 7})
		void fail_when_invalidScoreRange(int score) throws Exception {
			ReviewCreateRequest request = new ReviewCreateRequest(score, "comment", 100);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest()
				);
		}

		@DisplayName("comment의 크기가 100이 넘어가면 실패한다.")
		@Test
		void fail_when_commentLengthGreaterThan100() throws Exception {
			ReviewCreateRequest request = new ReviewCreateRequest(1, "a".repeat(101), 100);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest()
				);
		}
	}
}
