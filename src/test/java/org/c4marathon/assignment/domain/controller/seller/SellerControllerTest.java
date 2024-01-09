package org.c4marathon.assignment.domain.controller.seller;

import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.c4marathon.assignment.domain.controller.ControllerTestSupport;
import org.c4marathon.assignment.domain.seller.dto.request.PutProductRequest;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

public class SellerControllerTest extends ControllerTestSupport {

	@DisplayName("상품 업로드 시")
	@Nested
	class PutProduct {

		private static final String REQUEST_URL = "/sellers/products";

		@BeforeEach
		void setUp() {
			willDoNothing()
				.given(sellerService)
				.putProduct(any(PutProductRequest.class), any(Seller.class));
		}

		@DisplayName("올바른 요청을 하면 성공한다.")
		@Test
		void success_when_validRequest() throws Exception {
			PutProductRequest request = createRequest("name", "description", 100, 1000L);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isCreated(),
					content().string("success put product")
				);
		}

		@DisplayName("name이 null이거나 100자를 초과하면 실패한다.")
		@ParameterizedTest
		@MethodSource("provideNameArguments")
		void fail_when_nameIsNullOrExceed100(String name) throws Exception {
			PutProductRequest request = createRequest(name, "description", 100, 1000L);

			ErrorCode errorCode = BIND_ERROR;
			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.errorCode").value(errorCode.name()),
					jsonPath("$.message").value(errorCode.getMessage())
				);
		}

		@DisplayName("description이 null이거나 500자를 초과하면 실패한다.")
		@ParameterizedTest
		@MethodSource("provideDescriptionArguments")
		void fail_when_descriptionIsNullOrExceed500(String description) throws Exception {
			PutProductRequest request = createRequest("name", description, 100, 1000L);

			ErrorCode errorCode = BIND_ERROR;
			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.errorCode").value(errorCode.name()),
					jsonPath("$.message").value(errorCode.getMessage())
				);
		}

		@DisplayName("amount가 null이면 실패한다.")
		@Test
		void fail_when_amountIsNull() throws Exception {
			PutProductRequest request = createRequest("name", "description", 100, null);

			ErrorCode errorCode = BIND_ERROR;
			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.errorCode").value(errorCode.name()),
					jsonPath("$.message").value(errorCode.getMessage())
				);
		}

		@DisplayName("stock이 null이면 실패한다.")
		@Test
		void fail_when_stockIsNull() throws Exception {
			PutProductRequest request = createRequest("name", "description", null, 1000L);

			ErrorCode errorCode = BIND_ERROR;
			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.errorCode").value(errorCode.name()),
					jsonPath("$.message").value(errorCode.getMessage())
				);
		}

		private PutProductRequest createRequest(String name, String description, Integer stock, Long amount) {
			return new PutProductRequest(name, description, amount, stock);
		}

		private static Stream<Arguments> provideNameArguments() {
			return Stream.of(
				null,
				Arguments.of("a".repeat(101))
			);
		}

		private static Stream<Arguments> provideDescriptionArguments() {
			return Stream.of(
				null,
				Arguments.of("a".repeat(501))
			);
		}
	}
}
