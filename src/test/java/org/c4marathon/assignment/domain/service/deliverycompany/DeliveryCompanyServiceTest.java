package org.c4marathon.assignment.domain.service.deliverycompany;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.constant.DeliveryStatus.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.service.DeliveryReadService;
import org.c4marathon.assignment.domain.deliverycompany.dto.request.UpdateDeliveryStatusRequest;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.constant.DeliveryStatus;
import org.c4marathon.assignment.global.error.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class DeliveryCompanyServiceTest extends ServiceTestSupport {

	@Autowired
	private DeliveryCompanyService deliveryCompanyService;
	@MockBean
	private DeliveryCompanyReadService deliveryCompanyReadService;
	@MockBean
	private DeliveryReadService deliveryReadService;

	@DisplayName("회원 가입 시")
	@Nested
	class Signup {

		@DisplayName("가입된 email이 존재하지 않으면 성공한다.")
		@Test
		void success_when_emailNotExists() {
			SignUpRequest request = createRequest();

			given(deliveryCompanyReadService.existsByEmail(anyString()))
				.willReturn(false);

			deliveryCompanyService.signup(request);
			List<DeliveryCompany> deliveryCompanies = deliveryCompanyRepository.findAll();

			assertThat(deliveryCompanies).hasSize(1);
			assertThat(deliveryCompanies.get(0).getEmail()).isEqualTo(request.email());
		}

		@DisplayName("가입된 email이 존재하면 예외를 반환한다.")
		@Test
		void fail_when_emailExists() {
			SignUpRequest request = createRequest();

			given(deliveryCompanyReadService.existsByEmail(anyString()))
				.willReturn(true);

			BaseException exception = new BaseException(ALREADY_DELIVERY_COMPANY_EXISTS);
			assertThatThrownBy(() -> deliveryCompanyService.signup(request))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		private SignUpRequest createRequest() {
			return new SignUpRequest("email", "KOREA");
		}
	}

	@DisplayName("배송 상태 변경 시")
	@Nested
	class UpdateDeliveryStatus {

		private DeliveryCompany deliveryCompany;
		private Delivery delivery;

		@BeforeEach
		void setUp() {
			deliveryCompany = deliveryCompanyRepository.save(DeliveryCompany.builder()
				.email("email")
				.build());
			delivery = deliveryRepository.save(Delivery.builder()
				.address("ad")
				.deliveryCompany(deliveryCompany)
				.invoiceNumber("in")
				.build());
			given(deliveryReadService.findByIdJoinFetch(anyLong())).willReturn(delivery);
		}

		@DisplayName("올바른 상태에서 변경 요청은 성공한다.")
		@ParameterizedTest
		@CsvSource({"BEFORE_DELIVERY,IN_DELIVERY", "IN_DELIVERY,COMPLETE_DELIVERY"})
		void successUpdate_when_validStatus(DeliveryStatus before, DeliveryStatus after) {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(after);
			delivery.updateDeliveryStatus(before);

			deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany);

			assertThat(delivery.getDeliveryStatus()).isEqualTo(request.deliveryStatus());
		}

		@DisplayName("IN_DELIVERY가 아닌 상태에서 COMPLETE_DELIVERY로 변경하려하면 실패한다.")
		@Test
		void fail_when_updateCOMPLETE_DELIVERYWhenBEFORE_DELIVERY() {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(COMPLETE_DELIVERY);
			delivery.updateDeliveryStatus(BEFORE_DELIVERY);

			BaseException exception = new BaseException(INVALID_DELIVERY_STATUS_REQUEST);
			assertThatThrownBy(() -> deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("BEFORE_DELIVERY가 아닌 상태에서 IN_DELIVERY로 변경하려하면 실패한다.")
		@Test
		void fail_when_updateIN_DELIVERYWhenBEFORE_DELIVERY() {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(IN_DELIVERY);
			delivery.updateDeliveryStatus(COMPLETE_DELIVERY);

			BaseException exception = new BaseException(INVALID_DELIVERY_STATUS_REQUEST);
			assertThatThrownBy(() -> deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("변경할 상태가 BEFORE_DELIVERY이면 실패한다.")
		@Test
		void fail_when_statusIsBEFORE_DELIVERY() {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(BEFORE_DELIVERY);

			BaseException exception = new BaseException(INVALID_DELIVERY_STATUS_REQUEST);
			assertThatThrownBy(() -> deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("해당 delivery를 변경할 권한이 없으면 실패한다.")
		@Test
		void fail_when_noPermission() {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(BEFORE_DELIVERY);

			Delivery mockDelivery = mock(Delivery.class);
			DeliveryCompany mockDeliveryCompany = mock(DeliveryCompany.class);

			given(mockDelivery.getDeliveryCompany()).willReturn(mockDeliveryCompany);
			given(mockDeliveryCompany.getId()).willReturn(2L);
			given(deliveryReadService.findByIdJoinFetch(anyLong())).willReturn(mockDelivery);

			BaseException exception = new BaseException(NO_PERMISSION);
			assertThatThrownBy(() -> deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
