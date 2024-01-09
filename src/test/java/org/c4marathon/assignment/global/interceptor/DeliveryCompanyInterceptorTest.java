package org.c4marathon.assignment.global.interceptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.c4marathon.assignment.global.auth.DeliveryCompanyThreadLocal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DeliveryCompanyInterceptorTest extends InterceptorTestSupport {

	private final DeliveryCompanyInterceptor deliveryCompanyInterceptor;
	private final DeliveryCompanyRepository deliveryCompanyRepository;

	public DeliveryCompanyInterceptorTest() {
		this.deliveryCompanyRepository = mock(DeliveryCompanyRepository.class);
		this.deliveryCompanyInterceptor = new DeliveryCompanyInterceptor(deliveryCompanyRepository);
	}

	@DisplayName("preHandle 시")
	@Nested
	class PreHandle {

		@DisplayName("Authorization header에 email이 포함돼 있고, email에 해당하는 회원을 조회하면 true를 반환한다.")
		@Test
		void returnTrue_when_headerAndEmailIsValid() {
			given(request.getHeader(anyString()))
				.willReturn("email");
			DeliveryCompany deliveryCompany = mock(DeliveryCompany.class);
			given(deliveryCompanyRepository.findByEmail(anyString()))
				.willReturn(Optional.of(deliveryCompany));

			assertThat(deliveryCompanyInterceptor.preHandle(request, response, handler)).isTrue();
		}

		@DisplayName("Authorization header에 email이 포함돼 있지만, email에 해당하는 회원을 조회할 수 없으면 false를 반환한다.")
		@Test
		void returnFalse_when_EmailIsValid() {
			given(request.getHeader(anyString()))
				.willReturn("email");
			given(deliveryCompanyRepository.findByEmail(anyString()))
				.willReturn(Optional.empty());

			assertThat(deliveryCompanyInterceptor.preHandle(request, response, handler)).isFalse();
		}

		@DisplayName("Authorization header에 email이 포함돼 있지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_headerNotFound() {
			given(request.getHeader(anyString()))
				.willReturn(null);

			assertThat(deliveryCompanyInterceptor.preHandle(request, response, handler)).isFalse();
		}
	}

	@DisplayName("postHandle 시")
	@Nested
	class PostHandle {

		@DisplayName("DeliveryThreadLocal의 객체가 제거된다.")
		@Test
		void removeDeliveryCompanyThreadLocal_when_postHandle() throws Exception {
			DeliveryCompany deliveryCompany = mock(DeliveryCompany.class);
			DeliveryCompanyThreadLocal.set(deliveryCompany);

			deliveryCompanyInterceptor.postHandle(request, response, handler, modelAndView);

			assertThat(DeliveryCompanyThreadLocal.get()).isNull();
		}
	}
}
