package org.c4marathon.assignment.domain.controller.auth;

import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.controller.ControllerTestSupport;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

public class AuthControllerTest extends ControllerTestSupport {

	@DisplayName("회원 가입 시")
	@Nested
	class Signup {

		private static final String REQUEST_URL = "/auth/signup";

		@BeforeEach
		void setUp() {
			willDoNothing()
				.given(consumerService)
				.signup(any(SignUpRequest.class));
			willDoNothing()
				.given(sellerService)
				.signup(any(SignUpRequest.class));
			willDoNothing()
				.given(deliveryCompanyService)
				.signup(any(SignUpRequest.class));
		}

		@DisplayName("올바른 request와 memberType을 입력하면 회원가입에 성공한다.")
		@ParameterizedTest
		@ValueSource(strings = {"CONSUMER", "SELLER", "DELIVERY_COMPANY"})
		void successSignup_when_validRequest(String memberType) throws Exception {
			SignUpRequest request = createRequest("email@email.com");

			mockMvc.perform(post(REQUEST_URL)
					.param("memberType", memberType)
					.content(om.writeValueAsString(request))
					.characterEncoding(StandardCharsets.UTF_8)
					.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
					status().isCreated()
				);
		}

		@DisplayName("email이 null이면 예외를 반환한다.")
		@Test
		void throwException_when_emailIsNull() throws Exception {
			SignUpRequest request = createRequest(null);

			ErrorCode errorCode = BIND_ERROR;
			mockMvc.perform(post(REQUEST_URL)
					.param("memberType", "CONSUMER")
					.content(om.writeValueAsString(request))
					.characterEncoding(StandardCharsets.UTF_8)
					.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
					status().isBadRequest(),
					jsonPath("$.errorCode").value(errorCode.name()),
					jsonPath("$.message").value(errorCode.getMessage())
				);
		}

		private SignUpRequest createRequest(String email) {
			return new SignUpRequest(email, "KOREA");
		}
	}
}
