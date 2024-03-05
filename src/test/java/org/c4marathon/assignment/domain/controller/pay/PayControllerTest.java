package org.c4marathon.assignment.domain.controller.pay;

import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.c4marathon.assignment.domain.controller.ControllerTestSupport;
import org.c4marathon.assignment.domain.pay.dto.request.ChargePayRequest;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class PayControllerTest extends ControllerTestSupport {

	@DisplayName("캐시 충전 시")
	@Nested
	class ChargePay {

		private static final String REQUEST_URL = "/consumers/pay";

		@DisplayName("올바른 요청을 하면 성공한다.")
		@Test
		void success_when_validRequest() throws Exception {
			ChargePayRequest request = createRequest(10000L);

			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.characterEncoding(StandardCharsets.UTF_8)
					.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
					status().isOk()
				);
		}

		@DisplayName("amount가 0 미만이면 실패한다.")
		@Test
		void fail_when_amountIsNullOrLessThan0() throws Exception {
			ChargePayRequest request = createRequest(-1L);

			ErrorCode errorCode = BIND_ERROR;
			mockMvc.perform(post(REQUEST_URL)
					.content(om.writeValueAsString(request))
					.characterEncoding(StandardCharsets.UTF_8)
					.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.errorCode").value(errorCode.name()),
					jsonPath("$.message").value(errorCode.getMessage())
				);
		}

		private ChargePayRequest createRequest(Long amount) {
			return new ChargePayRequest(amount);
		}
	}
}
