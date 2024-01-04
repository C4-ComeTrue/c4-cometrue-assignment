package org.c4marathon.assignment.domain.controller.seller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.c4marathon.assignment.domain.controller.ControllerTestSupport;
import org.c4marathon.assignment.domain.seller.dto.request.PutProductRequest;
import org.c4marathon.assignment.domain.seller.entity.Seller;
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
					jsonPath("$.message").value("success put product")
				);
		}

		@DisplayName("name이 null이거나 100자를 초과하면 실패한다.")
		@ParameterizedTest
		@MethodSource("provideNameArguments")
		void fail_when_nameIsNullOrExceed100(String name, String message) throws Exception {
			PutProductRequest request = createRequest(name, "description", 100, 1000L);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.message").value(message)
				);
		}

		@DisplayName("description이 null이거나 500자를 초과하면 실패한다.")
		@ParameterizedTest
		@MethodSource("provideDescriptionArguments")
		void fail_when_descriptionIsNullOrExceed500(String description, String message) throws Exception {
			PutProductRequest request = createRequest("name", description, 100, 1000L);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.message").value(message)
				);
		}

		@DisplayName("amount가 null이면 실패한다.")
		@Test
		void fail_when_amountIsNull() throws Exception {
			PutProductRequest request = createRequest("name", "description", 100, null);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.message").value("amount is null")
				);
		}

		@DisplayName("stock이 null이면 실패한다.")
		@Test
		void fail_when_stockIsNull() throws Exception {
			PutProductRequest request = createRequest("name", "description", null, 1000L);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.message").value("stock is null")
				);
		}

		private PutProductRequest createRequest(String name, String description, Integer stock, Long amount) {
			return PutProductRequest.builder()
				.name(name)
				.description(description)
				.stock(stock)
				.amount(amount)
				.build();
		}

		private static Stream<Arguments> provideNameArguments() {
			return Stream.of(
				Arguments.of(null, "name is null"),
				Arguments.of("a".repeat(101), "name length exceed 100")
			);
		}

		private static Stream<Arguments> provideDescriptionArguments() {
			return Stream.of(
				Arguments.of(null, "description is null"),
				Arguments.of("a".repeat(501), "description length exceed 500")
			);
		}
	}
}
