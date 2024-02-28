package org.c4marathon.assignment.domain.service.delivery;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.delivery.service.DeliveryReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

public class DeliveryReadServiceTest extends ServiceTestSupport {

	@InjectMocks
	private DeliveryReadService deliveryReadService;

	@DisplayName("id로 조회 시")
	@Nested
	class FindByIdJoinFetch {

		@DisplayName("id에 해당하는 Delivery가 존재하면 반환한다.")
		@Test
		void returnDelivery_when_exists() {
			given(deliveryRepository.findByIdJoinFetch(anyLong())).willReturn(Optional.of(delivery));
			deliveryReadService.findByIdJoinFetch(1L);
			then(deliveryRepository)
				.should(times(1))
				.findByIdJoinFetch(anyLong());
		}

		@DisplayName("id에 해당하는 Delivery가 존재하면 예외를 반환한다.")
		@Test
		void throwException_when_notExists() {
			given(deliveryRepository.findByIdJoinFetch(anyLong()))
				.willReturn(Optional.empty());

			ErrorCode errorCode = DELIVERY_NOT_FOUND;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> deliveryReadService.findByIdJoinFetch(delivery.getId() + 1))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
