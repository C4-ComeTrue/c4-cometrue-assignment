package org.c4marathon.assignment.domain.service.order;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.service.OrderReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.constant.OrderStatus;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderReadServiceTest extends ServiceTestSupport {

	@Autowired
	private OrderReadService orderReadService;

	@DisplayName("id로 조회 시")
	@Nested
	class FindByIdJoinFetch {

		private Order order;

		@BeforeEach
		void setUp() {
			DeliveryCompany deliveryCompany = deliveryCompanyRepository.save(DeliveryCompany.builder()
				.email("email")
				.build());
			Delivery delivery = deliveryRepository.save(Delivery.builder()
				.deliveryCompany(deliveryCompany)
				.address("ad")
				.invoiceNumber("in")
				.build());
			Consumer consumer = consumerRepository.save(Consumer.builder()
				.address("ad")
				.email("email")
				.build());
			order = orderRepository.save(Order.builder()
				.orderStatus(OrderStatus.COMPLETE_PAYMENT)
				.delivery(delivery)
				.consumer(consumer)
				.build());
		}

		@DisplayName("id에 해당하는 주문이 존재하면 반환한다.")
		@Test
		void returnOrder_when_exists() {
			Order find = orderReadService.findByIdJoinFetch(this.order.getId());

			assertThat(find.getId()).isEqualTo(order.getId());
		}

		@DisplayName("id에 해당하는 주문이 존재하지 않으면 예외를 반환한다.")
		@Test
		void throwException_when_notExists() {
			ErrorCode errorCode = ORDER_NOT_FOUND;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());

			assertThatThrownBy(() -> orderReadService.findByIdJoinFetch(order.getId() + 1))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
