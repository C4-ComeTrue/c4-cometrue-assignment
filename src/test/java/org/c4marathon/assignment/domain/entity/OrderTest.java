package org.c4marathon.assignment.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.constant.OrderStatus.*;

import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.global.constant.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class OrderTest {

	@DisplayName("Order Entity test")
	@Nested
	class OrderEntityTest {

		private Order order;

		@BeforeEach
		void setUp() {
			order = Order.builder()
				.orderStatus(OrderStatus.COMPLETE_PAYMENT)
				.consumer(null)
				.usedPoint(0)
				.build();
		}

		@DisplayName("order entity의 메서드 수행 시, 필드가 변경된다.")
		@Test
		void updateField_when_invokeOrderEntityMethod() {
			order.updateOrderStatus(CONFIRM);
			order.updateDeliveryId(1L);
			order.updateEarnedPoint(10);
			order.updateTotalAmount(1000);

			assertThat(order.getOrderStatus()).isEqualTo(CONFIRM);
			assertThat(order.getDeliveryId()).isEqualTo(1L);
			assertThat(order.getEarnedPoint()).isEqualTo(10);
			assertThat(order.getTotalAmount()).isEqualTo(1000);
		}
	}
}
