package org.c4marathon.assignment.global.interceptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.global.auth.ConsumerThreadLocal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ConsumerInterceptorTest extends InterceptorTestSupport {

	private final ConsumerInterceptor consumerInterceptor;
	private final ConsumerRepository consumerRepository;

	public ConsumerInterceptorTest() {
		this.consumerRepository = mock(ConsumerRepository.class);
		this.consumerInterceptor = new ConsumerInterceptor(consumerRepository);
	}

	@DisplayName("preHandle 시")
	@Nested
	class PreHandle {

		@DisplayName("Authorization header에 email이 포함돼 있고, email에 해당하는 회원을 조회하면 true를 반환한다.")
		@Test
		void returnTrue_when_headerAndEmailIsValid() {
			given(request.getHeader(anyString()))
				.willReturn("email");
			Consumer consumer = mock(Consumer.class);
			given(consumerRepository.findByEmail(anyString()))
				.willReturn(Optional.of(consumer));

			assertThat(consumerInterceptor.preHandle(request, response, handler)).isTrue();
		}

		@DisplayName("Authorization header에 email이 포함돼 있지만, email에 해당하는 회원을 조회할 수 없으면 false를 반환한다.")
		@Test
		void returnFalse_when_EmailIsValid() {
			given(request.getHeader(anyString()))
				.willReturn("email");
			given(consumerRepository.findByEmail(anyString()))
				.willReturn(Optional.empty());

			assertThat(consumerInterceptor.preHandle(request, response, handler)).isFalse();
		}

		@DisplayName("Authorization header에 email이 포함돼 있지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_headerNotFound() {
			given(request.getHeader(anyString()))
				.willReturn(null);

			assertThat(consumerInterceptor.preHandle(request, response, handler)).isFalse();
		}
	}

	@DisplayName("postHandle 시")
	@Nested
	class PostHandle {

		@DisplayName("ConsumerThreadLocal의 객체가 제거된다.")
		@Test
		void removeConsumerThreadLocal_when_postHandle() throws Exception {
			Consumer consumer = mock(Consumer.class);
			ConsumerThreadLocal.set(consumer);

			consumerInterceptor.postHandle(request, response, handler, modelAndView);

			assertThat(ConsumerThreadLocal.get()).isNull();
		}
	}
}
