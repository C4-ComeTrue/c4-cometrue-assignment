package org.c4marathon.assignment.global.interceptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.c4marathon.assignment.global.auth.SellerThreadLocal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SellerInterceptorTest extends InterceptorTestSupport {

	private final SellerInterceptor sellerInterceptor;
	private final SellerRepository sellerRepository;

	public SellerInterceptorTest() {
		this.sellerRepository = mock(SellerRepository.class);
		this.sellerInterceptor = new SellerInterceptor(sellerRepository);
	}

	@DisplayName("preHandle 시")
	@Nested
	class PreHandle {

		@DisplayName("Authorization header에 email이 포함돼 있고, email에 해당하는 회원을 조회하면 true를 반환한다.")
		@Test
		void returnTrue_when_headerAndEmailIsValid() {
			given(request.getHeader(anyString()))
				.willReturn("email");
			Seller seller = mock(Seller.class);
			given(sellerRepository.findByEmail(anyString()))
				.willReturn(Optional.of(seller));

			assertThat(sellerInterceptor.preHandle(request, response, handler)).isTrue();
		}

		@DisplayName("Authorization header에 email이 포함돼 있지만, email에 해당하는 회원을 조회할 수 없으면 false를 반환한다.")
		@Test
		void returnFalse_when_EmailIsValid() {
			given(request.getHeader(anyString()))
				.willReturn("email");
			given(sellerRepository.findByEmail(anyString()))
				.willReturn(Optional.empty());

			assertThat(sellerInterceptor.preHandle(request, response, handler)).isFalse();
		}

		@DisplayName("Authorization header에 email이 포함돼 있지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_headerNotFound() {
			given(request.getHeader(anyString()))
				.willReturn(null);

			assertThat(sellerInterceptor.preHandle(request, response, handler)).isFalse();
		}
	}

	@DisplayName("postHandle 시")
	@Nested
	class PostHandle {

		@DisplayName("SellerThreadLocal의 객체가 제거된다.")
		@Test
		void removeSellerThreadLocal_when_postHandle() throws Exception {
			Seller seller = mock(Seller.class);
			SellerThreadLocal.set(seller);

			sellerInterceptor.postHandle(request, response, handler, modelAndView);

			assertThat(SellerThreadLocal.get()).isNull();
		}
	}
}
