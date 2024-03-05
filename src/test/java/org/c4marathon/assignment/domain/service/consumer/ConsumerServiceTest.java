package org.c4marathon.assignment.domain.service.consumer;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.constant.DeliveryStatus.*;
import static org.c4marathon.assignment.global.constant.OrderStatus.*;
import static org.c4marathon.assignment.global.constant.ProductStatus.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.service.ConsumerService;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.service.OrderReadService;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductJdbcRepository;
import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.constant.DeliveryStatus;
import org.c4marathon.assignment.global.constant.OrderStatus;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

public class ConsumerServiceTest extends ServiceTestSupport {

	@InjectMocks
	private ConsumerService consumerService;
	@Mock
	private ProductReadService productReadService;
	@Mock
	private OrderReadService orderReadService;
	@Mock
	private OrderProductReadService orderProductReadService;
	@Mock
	private DeliveryCompanyReadService deliveryCompanyReadService;
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;
	@Mock
	private OrderProductJdbcRepository orderProductJdbcRepository;

	@DisplayName("상품 구매 시")
	@Nested
	class PurchaseProduct {

		@BeforeEach
		void setUp() {
		}

		@DisplayName("사용할 포인트가 부족하면 실패한다.")
		@Test
		void should_fail_when_notEnoughPoint() {
			PurchaseProductRequest request = createRequest();
			Consumer consumer = mock(Consumer.class);
			given(consumer.getPoint()).willReturn(-1L);

			assertThatThrownBy(() -> consumerService.purchaseProduct(request, consumer))
				.isInstanceOf(BaseException.class)
				.hasMessage(NOT_ENOUGH_POINT.getMessage());
		}

		@DisplayName("balance가 부족하면 실패한다.")
		@Test
		void should_fail_when_notEnoughBalance() {
			PurchaseProductRequest request = createRequest();
			Consumer consumer = mock(Consumer.class);
			given(consumer.getBalance()).willReturn(-1L);
			given(consumer.getPoint()).willReturn(0L);
			Product product = mock(Product.class);
			given(productReadService.findById(anyLong()))
				.willReturn(product);
			given(product.getProductStatus()).willReturn(IN_STOCK);
			willDoNothing()
				.given(orderProductJdbcRepository)
				.saveAllBatch(anyList());

			assertThatThrownBy(() -> consumerService.purchaseProduct(request, consumer))
				.isInstanceOf(BaseException.class)
				.hasMessage(NOT_ENOUGH_BALANCE.getMessage());
		}

		@DisplayName("올바른 요청이 오면, order의 earnedPoint, totalAmount, delivery가 저장된다.")
		@Test
		void should_updateOrderEntity_when_validRequest() {
			Consumer consumer = mock(Consumer.class);
			given(consumer.getBalance()).willReturn(0L);
			given(consumer.getPoint()).willReturn(0L);
			Product product = mock(Product.class);
			given(productReadService.findById(anyLong()))
				.willReturn(product);
			given(product.getProductStatus()).willReturn(IN_STOCK);
			given(orderRepository.save(any(Order.class)))
				.willReturn(order);
			willDoNothing()
				.given(order)
				.updateEarnedPoint(anyLong());
			willDoNothing()
				.given(order)
				.updateTotalAmount(anyLong());
			willDoNothing()
				.given(order)
				.updateDelivery(any(Delivery.class));
			given(deliveryCompanyReadService.findMinimumCountOfDelivery())
				.willReturn(deliveryCompany);
			given(deliveryRepository.save(any(Delivery.class)))
				.willReturn(delivery);
			willDoNothing()
				.given(orderProductJdbcRepository)
				.saveAllBatch(anyList());
			consumerService.purchaseProduct(createRequest(), consumer);

			then(order)
				.should(times(1))
				.updateEarnedPoint(anyLong());
			then(order)
				.should(times(1))
				.updateTotalAmount(anyLong());
			then(order)
				.should(times(1))
				.updateDelivery(any(Delivery.class));
		}

		private PurchaseProductRequest createRequest() {
			return new PurchaseProductRequest(List.of(purchaseProductEntry), 0);
		}
	}

	@DisplayName("주문 환불 시")
	@Nested
	class RefundOrder {

		@DisplayName("배송 상태가 BEFORE_DELIVERY가 아니면 실패한다.")
		@Test
		void fail_when_deliveryStatusIsNotBefore_delivery() {
			given(orderReadService.findByIdJoinFetch(anyLong())).willReturn(order);
			given(delivery.getDeliveryStatus()).willReturn(DeliveryStatus.IN_DELIVERY);
			given(order.getConsumer()).willReturn(consumer);
			given(order.getDelivery()).willReturn(delivery);
			given(consumer.getId()).willReturn(1L);

			ErrorCode errorCode = REFUND_NOT_AVAILABLE;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.refundOrder(order.getId(), consumer))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("orderStatus가 수정되고 event를 발행한다.")
		@Test
		void updateOrderStatusAndDeliveryStatusAndBalance_when_refundOrder() {
			PointLog pointLog = mock(PointLog.class);
			given(orderReadService.findByIdJoinFetch(anyLong())).willReturn(order);
			given(delivery.getDeliveryStatus()).willReturn(DeliveryStatus.BEFORE_DELIVERY);
			given(order.getDelivery()).willReturn(delivery);
			given(order.getConsumer()).willReturn(consumer);
			given(consumer.getId()).willReturn(1L);
			given(pointLogRepository.save(any(PointLog.class)))
				.willReturn(pointLog);
			willDoNothing()
				.given(applicationEventPublisher)
				.publishEvent(any(PointLog.class));

			consumerService.refundOrder(order.getId(), consumer);

			then(order)
				.should(times(1))
				.updateOrderStatus(any(OrderStatus.class));
			then(applicationEventPublisher)
				.should(times(1))
				.publishEvent(any((PointLog.class)));
		}
	}

	@DisplayName("상품 구매 확정 시")
	@Nested
	class ConfirmOrder {

		@DisplayName("권한이 있으면서 올바른 배송, 주문 상태이면 성공한다.")
		@Test
		void confirmOrder_when_withPermissionAndValidStatus() {
			given(orderReadService.findByIdJoinFetch(anyLong())).willReturn(order);
			given(order.getConsumer()).willReturn(consumer);
			given(consumer.getId()).willReturn(1L);
			given(order.getOrderStatus()).willReturn(COMPLETE_PAYMENT);
			given(order.getDelivery()).willReturn(delivery);
			given(delivery.getDeliveryStatus()).willReturn(COMPLETE_DELIVERY);
			given(orderProduct.getProduct()).willReturn(product);
			given(product.getSeller()).willReturn(seller);
			given(orderProduct.getAmount()).willReturn(1000L);
			given(orderProductReadService.findByOrderJoinFetchProductAndSeller(anyLong()))
				.willReturn(List.of(orderProduct));
			PointLog pointLog = mock(PointLog.class);
			given(pointLogRepository.save(any(PointLog.class)))
				.willReturn(pointLog);

			consumerService.confirmOrder(1L, consumer);

			then(order)
				.should(times(1))
				.updateOrderStatus(any(OrderStatus.class));
			then(seller)
				.should(times(1))
				.addBalance(anyLong());
		}

		@DisplayName("권한이 없으면 실패한다.")
		@Test
		void throwException_when_noPermission() {
			given(orderReadService.findByIdJoinFetch(anyLong())).willReturn(order);
			given(order.getConsumer()).willReturn(consumer);
			given(consumer.getId()).willReturn(1L);
			Consumer mock = mock(Consumer.class);
			given(mock.getId()).willReturn(2L);

			ErrorCode errorCode = NO_PERMISSION;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.confirmOrder(1L, mock))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("올바르지 않은 주문 상태이면 실패한다.")
		@Test
		void fail_when_invalidOrderStatus() {
			given(orderReadService.findByIdJoinFetch(anyLong())).willReturn(order);
			given(order.getConsumer()).willReturn(consumer);
			given(consumer.getId()).willReturn(1L);
			given(order.getOrderStatus()).willReturn(CONFIRM);

			ErrorCode errorCode = CONFIRM_NOT_AVAILABLE;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.confirmOrder(1L, consumer))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("올바르지 않은 배송 상태이면 실패한다.")
		@Test
		void fail_when_invalidDeliveryStatus() {
			given(orderReadService.findByIdJoinFetch(anyLong())).willReturn(order);
			given(order.getConsumer()).willReturn(consumer);
			given(consumer.getId()).willReturn(1L);
			given(order.getOrderStatus()).willReturn(COMPLETE_PAYMENT);
			given(delivery.getDeliveryStatus()).willReturn(IN_DELIVERY);
			given(order.getDelivery()).willReturn(delivery);

			ErrorCode errorCode = CONFIRM_NOT_AVAILABLE;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.confirmOrder(1L, consumer))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
