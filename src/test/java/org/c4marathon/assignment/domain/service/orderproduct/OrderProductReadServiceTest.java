package org.c4marathon.assignment.domain.service.orderproduct;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;

import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

public class OrderProductReadServiceTest extends ServiceTestSupport {

	@InjectMocks
	private OrderProductReadService orderProductReadService;

	@DisplayName("id로 조회 시")
	@Nested
	class FindByOrderJoinFetchProduct {

		@DisplayName("orderId에 해당하는 OrderProduct가 모두 조회된다.")
		@Test
		void selectOrderProductsRelatedOrderId_when_findByOrderJoinFetchProduct() {
			given(orderProductRepository.findByOrderJoinFetchProduct(anyLong())).willReturn(List.of(orderProduct));
			List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetchProduct(1L);

			assertThat(orderProducts).hasSize(1);
			then(orderProductRepository)
				.should(times(1))
				.findByOrderJoinFetchProduct(anyLong());
		}

		@DisplayName("orderId에 해당하는 order가 없다면 빈 배열이 조회된다.")
		@Test
		void selectEmptyList_when_notExistsOrder() {
			given(orderProductRepository.findByOrderJoinFetchProduct(anyLong())).willReturn(Collections.emptyList());
			List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetchProduct(1L);

			assertThat(orderProducts).isEmpty();
			then(orderProductRepository)
				.should(times(1))
				.findByOrderJoinFetchProduct(anyLong());
		}
	}

	@DisplayName("id로 조회 시2")
	@Nested
	class FindByOrderJoinFetchProductAndSeller {

		@DisplayName("orderId에 해당하는 OrderProduct가 모두 조회된다.")
		@Test
		void selectOrderProductsRelatedOrderId_when_findByOrderJoinFetchProduct() {
			given(orderProductRepository.findByOrderJoinFetchProductAndSeller(anyLong())).willReturn(
				List.of(orderProduct));
			List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetchProductAndSeller(1L);

			assertThat(orderProducts).hasSize(1);
			then(orderProductRepository)
				.should(times(1))
				.findByOrderJoinFetchProductAndSeller(anyLong());
		}

		@DisplayName("orderId에 해당하는 order가 없다면 빈 배열이 조회된다.")
		@Test
		void selectEmptyList_when_notExistsOrder() {
			given(orderProductRepository.findByOrderJoinFetchProductAndSeller(anyLong())).willReturn(
				Collections.emptyList());
			List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetchProductAndSeller(1L);

			assertThat(orderProducts).isEmpty();
			then(orderProductRepository)
				.should(times(1))
				.findByOrderJoinFetchProductAndSeller(anyLong());
		}
	}
}
