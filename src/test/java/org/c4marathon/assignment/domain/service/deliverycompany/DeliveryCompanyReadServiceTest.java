package org.c4marathon.assignment.domain.service.deliverycompany;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

public class DeliveryCompanyReadServiceTest extends ServiceTestSupport {

	@InjectMocks
	private DeliveryCompanyReadService deliveryCompanyReadService;

	@DisplayName("이메일로 배송 회사 조회 시")
	@Nested
	class ExistsByEmail {

		@DisplayName("email에 해당하는 회원이 존재하면 true를 반환한다.")
		@Test
		void returnTrue_when_existsDeliveryCompany() {
			given(deliveryCompanyRepository.existsByEmail(anyString())).willReturn(true);
			assertThat(deliveryCompanyReadService.existsByEmail("email")).isTrue();
		}

		@DisplayName("email에 해당하는 회원이 존재하지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_notExistsDeliveryCompany() {
			given(deliveryCompanyRepository.existsByEmail(anyString())).willReturn(false);
			assertThat(deliveryCompanyReadService.existsByEmail("email")).isFalse();
		}
	}

	@DisplayName("최소 배송 정보를 가진 배송 회사 조회 시")
	@Nested
	class FindMinimumCountOfDelivery {

		@DisplayName("배송 정보가 1, 2개인 배송 회사가 존재하면, 배송 정보가 1개인 배송 회사가 조회된다.")
		@Test
		void selectOneDeliveryInfo_when_oneAndTwoInfoExists() {
			given(deliveryCompanyRepository.findMinimumCountOfDelivery()).willReturn(Optional.of(deliveryCompany));
			deliveryCompanyReadService.findMinimumCountOfDelivery();
			then(deliveryCompanyRepository)
				.should(times(1))
				.findMinimumCountOfDelivery();
		}

		@DisplayName("배송회사가 존재하지 않으면 예외를 반환한다.")
		@Test
		void throwException_when_notExistsCompany() {
			given(deliveryCompanyRepository.findMinimumCountOfDelivery()).willReturn(Optional.empty());

			ErrorCode errorCode = DELIVERY_COMPANY_NOT_FOUND;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> deliveryCompanyReadService.findMinimumCountOfDelivery())
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
