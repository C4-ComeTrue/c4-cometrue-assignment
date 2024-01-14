package org.c4marathon.assignment.domain.consumer.service;

import static org.c4marathon.assignment.global.constant.DeliveryStatus.*;
import static org.c4marathon.assignment.global.constant.OrderStatus.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjLongConsumer;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductEntry;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.repository.DeliveryRepository;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.repository.OrderRepository;
import org.c4marathon.assignment.domain.order.service.OrderReadService;
import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductJdbcRepository;
import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumerService {

	public static final double FEE = 0.05;
	public static final String INVOICE_NUMBER_PATTERN = "yyyyMMddHHmmssSSSSSSSSS";
	private final ConsumerRepository consumerRepository;
	private final ConsumerReadService consumerReadService;
	private final OrderRepository orderRepository;
	private final DeliveryRepository deliveryRepository;
	private final ProductReadService productReadService;
	private final OrderProductJdbcRepository orderProductJdbcRepository;
	private final OrderReadService orderReadService;
	private final OrderProductReadService orderProductReadService;
	private final DeliveryCompanyReadService deliveryCompanyReadService;

	/**
	 * 회원 가입
	 * @param request address와 email
	 */
	public void signup(SignUpRequest request) {
		if (request.address() == null) {
			throw CONSUMER_NEED_ADDRESS.baseException();
		}
		processSignup(request);
	}

	/**
	 * 상품 구매
	 * Delivery 생성 -> Order 생성 -> OrderProduct 생성
	 * @param consumer 상품 구매하는 소비자
	 */
	@Transactional
	public void purchaseProduct(PurchaseProductRequest request, Consumer consumer) {
		Delivery delivery = saveDelivery(consumer);
		Order order = saveOrder(consumer, delivery);
		long totalAmount = saveOrderProduct(request, order);
		if (consumer.getBalance() < totalAmount) {
			throw NOT_ENOUGH_BALANCE.baseException("total amount: %d", totalAmount);
		}
		updateConsumerBalance(consumer, totalAmount, (c, a) -> c.decreaseBalance(totalAmount));
	}

	/**
	 * 상품 환불
	 * @param orderId 환불하려는 Order PK
	 * @param consumer 환불하려는 소비자
	 */
	@Transactional
	public void refundOrder(Long orderId, Consumer consumer) {
		Order order = orderReadService.findByIdJoinFetch(orderId);
		validateRefundRequest(consumer, order);

		updateStatusWhenRefund(order);
		long totalAmount = orderProductReadService.findTotalAmountByOrderId(orderId);
		updateConsumerBalance(consumer, totalAmount, (c, a) -> consumer.addBalance(totalAmount));
	}

	/**
	 * 상품 구매 확정
	 * @param orderId 구매 확정하려는 Order PK
	 * @param consumer 구매 확정하려는 소비자
	 */
	@Transactional
	public void confirmOrder(Long orderId, Consumer consumer) {
		Order order = orderReadService.findByIdJoinFetch(orderId);
		validateConfirmRequest(consumer, order);

		order.updateOrderStatus(CONFIRM);
		List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetchProductAndSeller(orderId);

		addSellerBalance(orderProducts);
	}

	/**
	 * Delivery 저장
	 */
	private Delivery saveDelivery(Consumer consumer) {
		return deliveryRepository.save(Delivery.builder()
			.address(consumer.getAddress())
			.invoiceNumber(createInvoiceNumber(consumer))
			.deliveryCompany(deliveryCompanyReadService.findMinimumCountOfDelivery())
			.build());
	}

	/**
	 * Order 저장
	 */
	private Order saveOrder(Consumer consumer, Delivery delivery) {
		return orderRepository.save(Order.builder()
			.orderStatus(COMPLETE_PAYMENT)
			.consumer(consumer)
			.delivery(delivery)
			.build());
	}

	/**
	 * Product의 stock 감소
	 * Order에 대한 OrderProduct를 저장
	 * @return 총 주문 금액
	 */
	private long saveOrderProduct(PurchaseProductRequest request, Order order) {
		List<OrderProduct> orderProducts = new ArrayList<>();
		long totalAmount = 0;
		for (PurchaseProductEntry purchaseProductEntry : request.purchaseProducts()) {
			Product product = productReadService.findById(purchaseProductEntry.productId());
			product.decreaseStock(purchaseProductEntry.quantity());

			OrderProduct orderProduct = createOrderProduct(order, purchaseProductEntry, product);
			orderProducts.add(orderProduct);
			totalAmount += orderProduct.getAmount();
		}
		orderProductJdbcRepository.saveAllBatch(orderProducts);
		return totalAmount;
	}

	/**
	 * 소비자의 잔고 감소 또는 증가
	 * @param totalAmount 감소 또는 증가할 총 주문 금액
	 * @param updater 감소 또는 증가 작업을 결정 하는 interface
	 */
	private void updateConsumerBalance(Consumer consumer, long totalAmount, ObjLongConsumer<Consumer> updater) {
		updater.accept(consumer, totalAmount);
		consumerRepository.save(consumer);
	}

	/**
	 * 소비자의 잔고 증가
	 * @param orderProducts 구매할 OrderProdct list
	 */
	private void addSellerBalance(List<OrderProduct> orderProducts) {
		for (OrderProduct orderProduct : orderProducts) {
			long amount = getSalesAmountExcludeFee(orderProduct);
			Seller seller = orderProduct.getProduct().getSeller();
			seller.addBalance(amount);
		}
	}

	/**
	 * @return 수수료 5%를 제외한 이익
	 */
	private long getSalesAmountExcludeFee(OrderProduct orderProduct) {
		return (long)(orderProduct.getAmount() * (1 - FEE));
	}

	/**
	 * 환불 시 Order, Delivery의 상태를 변경
	 * @param order 환불할 Order
	 */
	private void updateStatusWhenRefund(Order order) {
		order.getDelivery().updateDeliveryStatus(COMPLETE_DELIVERY);
		order.updateOrderStatus(REFUND);
	}

	/**
	 * email 중복 체크 이후 Consumer 저장
	 */
	private void processSignup(SignUpRequest request) {
		if (Boolean.TRUE.equals(consumerReadService.existsByEmail(request.email()))) {
			throw ALREADY_CONSUMER_EXISTS.baseException("email: %s", request.email());
		}
		saveConsumer(request);
	}

	/**
	 * 구매 확정 시 검증 과정
	 * 1. 주문한 소비자가 현재 로그인한 소비자인지
	 * 2. 올바른 주문, 배송 상태인지
	 */
	private void validateConfirmRequest(Consumer consumer, Order order) {
		if (!order.getConsumer().getId().equals(consumer.getId())) {
			throw NO_PERMISSION.baseException();
		}
		if (!order.getOrderStatus().equals(COMPLETE_PAYMENT)
			|| !order.getDelivery().getDeliveryStatus().equals(COMPLETE_DELIVERY)) {
			throw CONFIRM_NOT_AVAILABLE.baseException("current status: %s", order.getOrderStatus());
		}
	}

	/**
	 * 환불 시 검증 과정
	 * 1. 주문한 소비자가 현재 로그인한 소비자인지
	 * 2. 올바른 배송 상태인지
	 */
	private void validateRefundRequest(Consumer consumer, Order order) {
		if (!order.getConsumer().getId().equals(consumer.getId())) {
			throw NO_PERMISSION.baseException();
		}
		if (!order.getDelivery().getDeliveryStatus().equals(BEFORE_DELIVERY)) {
			throw REFUND_NOT_AVAILABLE.baseException("delivery status: %s",
				order.getDelivery().getDeliveryStatus().toString());
		}
	}

	/**
	 * OrderProduct 저장
	 */
	private OrderProduct createOrderProduct(Order order, PurchaseProductEntry purchaseProductEntry, Product product) {
		return OrderProduct.builder()
			.quantity(purchaseProductEntry.quantity())
			.amount(product.getAmount() * purchaseProductEntry.quantity())
			.order(order)
			.product(product)
			.build();
	}

	/**
	 * Consumer 저장
	 */
	private void saveConsumer(SignUpRequest request) {
		consumerRepository.save(new Consumer(request.email(), request.address()));
	}

	/**
	 * 송장번호 생성
	 * 현재 시간(nano second) + consumer PK
	 */
	private String createInvoiceNumber(Consumer consumer) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(INVOICE_NUMBER_PATTERN);
		return now.format(formatter) + consumer.getId();
	}
}
