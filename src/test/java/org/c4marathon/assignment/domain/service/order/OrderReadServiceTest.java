package org.c4marathon.assignment.domain.service.order;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.order.service.OrderReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

public class OrderReadServiceTest extends ServiceTestSupport {

	@InjectMocks
	private OrderReadService orderReadService;

	@DisplayName("id로 조회 시")
	@Nested
	class FindByIdJoinFetch {

		@DisplayName("id에 해당하는 주문이 존재하면 반환한다.")
		@Test
		void returnOrder_when_exists() {
			given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));
			orderReadService.findById(1L);
			then(orderRepository)
				.should(times(1))
				.findById(anyLong());
		}

		@DisplayName("id에 해당하는 주문이 존재하지 않으면 예외를 반환한다.")
		@Test
		void throwException_when_notExists() {
			given(orderRepository.findById(anyLong())).willReturn(Optional.empty());
			ErrorCode errorCode = ORDER_NOT_FOUND;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());

			assertThatThrownBy(() -> orderReadService.findById(order.getId() + 1))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
