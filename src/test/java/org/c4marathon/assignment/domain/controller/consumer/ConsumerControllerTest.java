package org.c4marathon.assignment.domain.controller.consumer;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductEntry;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.controller.ControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class ConsumerControllerTest extends ControllerTestSupport {

	@DisplayName("상품 구매 시")
	@Nested
	class PurchaseProduct {

		private static final String REQUEST_URL = "/consumers/orders";

		@BeforeEach
		void setUp() {
			willDoNothing()
				.given(consumerService)
				.purchaseProduct(any(PurchaseProductRequest.class), any(Consumer.class));
		}

		@DisplayName("올바른 request를 요청하면 성공한다.")
		@Test
		void successPurchase_when_validRequest() throws Exception {
			PurchaseProductRequest request = createRequest(1L, 1);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isOk(),
					jsonPath("$.message").value("success purchase product")
				);
		}

		@DisplayName("productId가 null이면 실패한다.")
		@Test
		void fail_when_productIdIsNull() throws Exception {
			PurchaseProductRequest request = createRequest(null, 1);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.message").value("productId is null")
				);
		}

		@DisplayName("quantity가 null이면 실패한다.")
		@Test
		void fail_when_quantityIsNull() throws Exception {
			PurchaseProductRequest request = createRequest(1L, null);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.message").value("quantity is null")
				);
		}

		@DisplayName("quantity가 1미만이면 실패한다.")
		@Test
		void fail_when_quantityIsLessThan1() throws Exception {
			PurchaseProductRequest request = createRequest(1L, 0);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.message").value("quantity is less than 1")
				);
		}

		private PurchaseProductRequest createRequest(Long productId, Integer quantity) {
			return PurchaseProductRequest.builder()
				.purchaseProducts(List.of(
					PurchaseProductEntry.builder()
						.productId(productId)
						.quantity(quantity)
						.build()))
				.build();
		}
	}

	@DisplayName("상품 환불 시")
	@Nested
	class RefundProduct {

		private static final String REQUEST_URL = "/consumers/orders/{order_id}";

		@BeforeEach
		void setUp() {
			willDoNothing()
				.given(consumerService)
				.refundOrder(any(Long.TYPE), any(Consumer.class));
		}

		@DisplayName("올바른 orderId를 입력하면 성공한다.")
		@Test
		void success_when_validOrderID() throws Exception {
			mockMvc.perform(delete(REQUEST_URL, 1L))
				.andExpectAll(
					status().isOk(),
					jsonPath("$.message").value("success refund product")
				);
		}

		@DisplayName("path variable의 타입이 올바르지 않으면 실패한다.")
		@Test
		void fail_when_invalidOrderIdType() throws Exception {
			mockMvc.perform(delete(REQUEST_URL, "a"))
				.andExpectAll(
					status().isBadRequest()
				);
		}
	}

	@DisplayName("상품 구매 확정 시")
	@Nested
	class ConfirmOrder {

		private static final String REQUEST_URL = "/consumers/orders/{order_id}";

		@BeforeEach
		void setUp() {
			willDoNothing()
				.given(consumerService)
				.confirmOrder(any(Long.TYPE), any(Consumer.class));
		}

		@DisplayName("올바른 orderId를 입력하면 성공한다.")
		@Test
		void success_when_validOrderID() throws Exception {
			mockMvc.perform(patch(REQUEST_URL, 1L))
				.andExpectAll(
					status().isOk(),
					jsonPath("$.message").value("success confirm order")
				);
		}

		@DisplayName("path variable의 타입이 올바르지 않으면 실패한다.")
		@Test
		void fail_when_invalidOrderIdType() throws Exception {
			mockMvc.perform(patch(REQUEST_URL, "a"))
				.andExpectAll(
					status().isBadRequest()
				);
		}
	}
}
