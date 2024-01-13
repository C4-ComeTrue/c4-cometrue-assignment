package org.c4marathon.assignment.domain.controller.deliverycompany;

import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.c4marathon.assignment.domain.controller.ControllerTestSupport;
import org.c4marathon.assignment.domain.deliverycompany.dto.request.UpdateDeliveryStatusRequest;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.global.constant.DeliveryStatus;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;

public class DeliveryCompanyControllerTest extends ControllerTestSupport {

	@DisplayName("배송 상태 변경 시")
	@Nested
	class UpdateDeliveryStatus {

		private static final String REQUEST_URL = "/orders/deliveries/{delivery_id}/status";

		@BeforeEach
		void setUp() {
			willDoNothing()
				.given(deliveryCompanyService)
				.updateDeliveryStatus(any(Long.TYPE), any(UpdateDeliveryStatusRequest.class),
					any(DeliveryCompany.class));
		}

		@DisplayName("올바른 request를 요청하면 성공한다.")
		@ParameterizedTest
		@EnumSource(value = DeliveryStatus.class)
		void success_when_validRequest(DeliveryStatus deliveryStatus) throws Exception {
			UpdateDeliveryStatusRequest request = createRequest(deliveryStatus);

			mockMvc.perform(patch(REQUEST_URL, 1L)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isOk()
				);
		}

		@DisplayName("deliveryStatus가 null이면 실패한다.")
		@Test
		void fail_when_deliveryStatusIsNull() throws Exception {
			UpdateDeliveryStatusRequest request = createRequest(null);

			ErrorCode errorCode = BIND_ERROR;
			mockMvc.perform(patch(REQUEST_URL, 1L)
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.errorCode").value(errorCode.name()),
					jsonPath("$.message").value(errorCode.getMessage())
				);
		}

		@DisplayName("path variable의 타입이 올바르지 않으면 실패한다.")
		@Test
		void fail_when_invalidPathVariable() throws Exception {
			UpdateDeliveryStatusRequest request = createRequest(DeliveryStatus.IN_DELIVERY);

			mockMvc.perform(patch(REQUEST_URL, "a")
					.content(om.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8))
				.andExpectAll(
					status().isBadRequest()
				);
		}

		private UpdateDeliveryStatusRequest createRequest(DeliveryStatus deliveryStatus) {
			return new UpdateDeliveryStatusRequest(deliveryStatus);
		}
	}
}
