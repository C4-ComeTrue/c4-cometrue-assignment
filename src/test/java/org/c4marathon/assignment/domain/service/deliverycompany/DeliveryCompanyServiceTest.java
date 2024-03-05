package org.c4marathon.assignment.domain.service.deliverycompany;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.constant.DeliveryStatus.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.domain.delivery.service.DeliveryReadService;
import org.c4marathon.assignment.domain.deliverycompany.dto.request.UpdateDeliveryStatusRequest;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.constant.DeliveryStatus;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class DeliveryCompanyServiceTest extends ServiceTestSupport {

	@InjectMocks
	private DeliveryCompanyService deliveryCompanyService;
	@Mock
	private DeliveryReadService deliveryReadService;

	@DisplayName("배송 상태 변경 시")
	@Nested
	class UpdateDeliveryStatus {
		@BeforeEach
		void setUp() {
			given(deliveryReadService.findByIdJoinFetch(anyLong())).willReturn(delivery);
			given(delivery.getDeliveryCompany()).willReturn(deliveryCompany);
			given(deliveryCompany.getId()).willReturn(1L);
		}

		@DisplayName("올바른 상태에서 변경 요청은 성공한다.")
		@ParameterizedTest
		@CsvSource({"BEFORE_DELIVERY,IN_DELIVERY", "IN_DELIVERY,COMPLETE_DELIVERY"})
		void successUpdate_when_validStatus(DeliveryStatus before, DeliveryStatus after) {
			given(delivery.getDeliveryStatus()).willReturn(before);

			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(after);
			deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany);

			then(delivery)
				.should(times(1))
				.updateDeliveryStatus(any(DeliveryStatus.class));
		}

		@DisplayName("IN_DELIVERY가 아닌 상태에서 COMPLETE_DELIVERY로 변경하려하면 실패한다.")
		@Test
		void fail_when_updateCompleteDelivery_when_BeforeDelivery() {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(COMPLETE_DELIVERY);
			given(delivery.getDeliveryStatus()).willReturn(BEFORE_DELIVERY);

			ErrorCode errorCode = INVALID_DELIVERY_STATUS_REQUEST;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("BEFORE_DELIVERY가 아닌 상태에서 IN_DELIVERY로 변경하려하면 실패한다.")
		@Test
		void fail_when_updateInDelivery_when_BeforeDelivery() {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(IN_DELIVERY);
			given(delivery.getDeliveryStatus()).willReturn(COMPLETE_DELIVERY);

			ErrorCode errorCode = INVALID_DELIVERY_STATUS_REQUEST;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("변경할 상태가 BEFORE_DELIVERY이면 실패한다.")
		@Test
		void fail_when_statusIsBeforeDelivery() {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(BEFORE_DELIVERY);
			given(delivery.getDeliveryStatus()).willReturn(BEFORE_DELIVERY);

			ErrorCode errorCode = INVALID_DELIVERY_STATUS_REQUEST;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("해당 delivery를 변경할 권한이 없으면 실패한다.")
		@Test
		void fail_when_noPermission() {
			UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(BEFORE_DELIVERY);
			DeliveryCompany mock = mock(DeliveryCompany.class);
			given(delivery.getDeliveryCompany()).willReturn(mock);
			given(mock.getId()).willReturn(2L);

			ErrorCode errorCode = NO_PERMISSION;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> deliveryCompanyService.updateDeliveryStatus(1L, request, deliveryCompany))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
