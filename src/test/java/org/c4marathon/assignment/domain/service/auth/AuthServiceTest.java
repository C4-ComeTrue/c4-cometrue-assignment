package org.c4marathon.assignment.domain.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.auth.service.AuthService;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.service.ConsumerReadService;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.service.SellerReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.constant.MemberType;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class AuthServiceTest extends ServiceTestSupport {

	@InjectMocks
	private AuthService authService;
	@Mock
	private ConsumerReadService consumerReadService;
	@Mock
	private SellerReadService sellerReadService;
	@Mock
	private DeliveryCompanyReadService deliveryCompanyReadService;

	@DisplayName("회원가입 시")
	@Nested
	class Signup {

		@DisplayName("소비자 회원가입 시, address가 null이면 실패한다.")
		@Test
		void should_fail_when_addressIsNull() {
			SignUpRequest request = new SignUpRequest("email", null);

			assertThatThrownBy(() -> authService.signup(request, MemberType.CONSUMER))
				.isInstanceOf(BaseException.class)
				.hasMessage(ErrorCode.CONSUMER_NEED_ADDRESS.getMessage());
		}

		@DisplayName("소비자 회원가입 시, 중복된 email이 존재하면 실패한다.")
		@Test
		void should_fail_when_consumerEmailIsDuplicated() {
			SignUpRequest request = new SignUpRequest("email", "address");

			given(consumerReadService.existsByEmail(anyString()))
				.willReturn(true);

			assertThatThrownBy(() -> authService.signup(request, MemberType.CONSUMER))
				.isInstanceOf(BaseException.class)
				.hasMessage(ErrorCode.ALREADY_CONSUMER_EXISTS.getMessage());
		}

		@DisplayName("판매자 회원가입 시, 중복된 email이 존재하면 실패한다.")
		@Test
		void should_fail_when_sellerEmailIsDuplicated() {
			SignUpRequest request = new SignUpRequest("email", "address");

			given(sellerReadService.existsByEmail(anyString()))
				.willReturn(true);

			assertThatThrownBy(() -> authService.signup(request, MemberType.SELLER))
				.isInstanceOf(BaseException.class)
				.hasMessage(ErrorCode.ALREADY_SELLER_EXISTS.getMessage());
		}

		@DisplayName("배송회사 회원가입 시, 중복된 email이 존재하면 실패한다.")
		@Test
		void should_fail_when_deliveryCompanyEmailIsDuplicated() {
			SignUpRequest request = new SignUpRequest("email", "address");

			given(deliveryCompanyReadService.existsByEmail(anyString()))
				.willReturn(true);

			assertThatThrownBy(() -> authService.signup(request, MemberType.DELIVERY_COMPANY))
				.isInstanceOf(BaseException.class)
				.hasMessage(ErrorCode.ALREADY_DELIVERY_COMPANY_EXISTS.getMessage());
		}

		@DisplayName("올바른 요청이 오면, 회원가입에 성공한다.")
		@ParameterizedTest
		@EnumSource(value = MemberType.class)
		void should_success_when_validRequest(MemberType memberType) {
			SignUpRequest request = new SignUpRequest("email", "address");

			switch (memberType) {
				case CONSUMER -> given(consumerReadService.existsByEmail(anyString())).willReturn(false);
				case SELLER -> given(sellerReadService.existsByEmail(anyString())).willReturn(false);
				case DELIVERY_COMPANY -> given(deliveryCompanyReadService.existsByEmail(anyString())).willReturn(false);
			}

			authService.signup(request, memberType);

			switch (memberType) {
				case CONSUMER -> then(consumerRepository).should(times(1)).save(any(Consumer.class));
				case SELLER -> then(sellerRepository).should(times(1)).save(any(Seller.class));
				case DELIVERY_COMPANY ->
					then(deliveryCompanyRepository).should(times(1)).save(any(DeliveryCompany.class));
			}
		}
	}
}
