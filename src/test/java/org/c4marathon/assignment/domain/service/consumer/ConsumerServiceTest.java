package org.c4marathon.assignment.domain.service.consumer;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.constant.DeliveryStatus.*;
import static org.c4marathon.assignment.global.constant.OrderStatus.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.service.ConsumerReadService;
import org.c4marathon.assignment.domain.consumer.service.ConsumerService;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.service.OrderReadService;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductJdbcRepository;
import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
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

public class ConsumerServiceTest extends ServiceTestSupport {

	@Mock
	private ConsumerReadService consumerReadService;
	@Mock
	private DeliveryCompanyReadService deliveryCompanyReadService;
	@InjectMocks
	private ConsumerService consumerService;
	@Mock
	private ProductReadService productReadService;
	@Mock
	private OrderProductJdbcRepository orderProductJdbcRepository;
	@Mock
	private OrderReadService orderReadService;
	@Mock
	private OrderProductReadService orderProductReadService;

	@DisplayName("회원가입 시")
	@Nested
	class Signup {

		@DisplayName("address가 null이 아니고, 가입된 email이 존재하지 않는다면 성공한다.")
		@Test
		void success_when_addressIsNotNullAndEmailNotExists() {
			SignUpRequest request = createRequest("address");

			given(consumerReadService.existsByEmail(anyString())).willReturn(false);

			assertThatNoException().isThrownBy(() -> consumerService.signup(request));
			then(consumerRepository)
				.should(times(1))
				.save(any(Consumer.class));
		}

		@DisplayName("address가 null이면 예외를 반환한다.")
		@Test
		void throwException_when_addressIsNull() {
			SignUpRequest request = createRequest(null);

			ErrorCode errorCode = CONSUMER_NEED_ADDRESS;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());

			assertThatThrownBy(() -> consumerService.signup(request))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("이미 회원가입된 이메일이 존재하면 예외를 반환한다.")
		@Test
		void throwException_when_alreadyExistsEmail() {
			SignUpRequest request = createRequest("address");

			given(consumerReadService.existsByEmail(anyString())).willReturn(true);

			ErrorCode errorCode = ALREADY_CONSUMER_EXISTS;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.signup(request))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		private SignUpRequest createRequest(String address) {
			return new SignUpRequest("email123", address);
		}
	}

	@DisplayName("상품 구매 시")
	@Nested
	class PurchaseProduct {
		private final Long AMOUNT = 1000L;

		@BeforeEach
		void setUp() {
			given(deliveryCompanyReadService.findMinimumCountOfDelivery()).willReturn(deliveryCompany);
			given(productReadService.findById(anyLong())).willReturn(product);
			given(product.getAmount()).willReturn(AMOUNT);
			given(purchaseProductEntry.quantity()).willReturn(1);
			given(consumer.getBalance()).willReturn(AMOUNT);
		}

		@DisplayName("각 productId와 quantity를 요청하면 Order가 생성된다.")
		@Test
		void createOrder_when_validRequest() {
			PurchaseProductRequest request = createRequest();
			consumerService.purchaseProduct(request, consumer);

			then(deliveryRepository)
				.should(times(1))
				.save(any(Delivery.class));
			then(orderRepository)
				.should(times(1))
				.save(any(Order.class));
			then(orderProductJdbcRepository)
				.should(times(1))
				.saveAllBatch(anyList());
			then(consumer)
				.should(times(1))
				.decreaseBalance(AMOUNT);
			then(product)
				.should(times(1))
				.decreaseStock(anyInt());
		}

		@DisplayName("구매자의 캐시가 부족하다면 실패한다.")
		@Test
		void fail_when_balanceIsLessThanTotalAmount() {
			long amount = 1000L;
			PurchaseProductRequest request = createRequest();

			given(product.getAmount()).willReturn(amount + 1);

			ErrorCode errorCode = NOT_ENOUGH_BALANCE;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.purchaseProduct(request, consumer))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		private PurchaseProductRequest createRequest() {
			return new PurchaseProductRequest(List.of(purchaseProductEntry));
		}
	}

	@DisplayName("주문 환불 시")
	@Nested
	class RefundOrder {

		@DisplayName("배송 상태가 BEFORE_DELIVERY가 아니면 실패한다.")
		@Test
		void fail_when_deliveryStatusIsNotBEFORE_DELIVRY() {
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

		@DisplayName("orderStatus, deliveryStatus와 Consumer의 balance가 수정된다.")
		@Test
		void updateOrderStatusAndDeliveryStatusAndBalance_when_refundOrder() {
			given(orderReadService.findByIdJoinFetch(anyLong())).willReturn(order);
			given(delivery.getDeliveryStatus()).willReturn(DeliveryStatus.BEFORE_DELIVERY);
			given(order.getDelivery()).willReturn(delivery);
			given(order.getConsumer()).willReturn(consumer);
			given(consumer.getId()).willReturn(1L);
			given(orderProductReadService.findByOrderJoinFetchProduct(anyLong())).willReturn(List.of(orderProduct));
			given(orderProduct.getProduct()).willReturn(product);
			given(orderProduct.getAmount()).willReturn(1000L);

			consumerService.refundOrder(order.getId(), consumer);

			then(order)
				.should(times(1))
				.updateOrderStatus(any(OrderStatus.class));
			then(consumer)
				.should(times(1))
				.addBalance(anyLong());
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
			given(orderProduct.getQuantity()).willReturn(1);
			given(orderProduct.getAmount()).willReturn(1000L);
			given(orderProductReadService.findByOrderJoinFetchProductAndSeller(anyLong()))
				.willReturn(List.of(orderProduct));

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
