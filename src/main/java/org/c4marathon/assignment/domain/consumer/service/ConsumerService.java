package org.c4marathon.assignment.domain.consumer.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductEntry;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.repository.DeliveryRepository;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.repository.OrderRepository;
import org.c4marathon.assignment.domain.order.service.OrderReadService;
import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductJdbcRepository;
import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.global.constant.DeliveryStatus;
import org.c4marathon.assignment.global.constant.OrderStatus;
import org.c4marathon.assignment.global.error.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
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

	public void signup(SignUpRequest request) {
		if (request.getAddress() == null) {
			throw new BaseException(CONSUMER_NEED_ADDRESS);
		}
		if (consumerReadService.existsByEmail(request.getEmail())) {
			throw new BaseException(ALREADY_CONSUMER_EXISTS);
		}

		saveConsumer(request);
	}

	public void purchaseProduct(PurchaseProductRequest request, Consumer consumer) {
		Delivery delivery = saveDelivery(consumer);
		Order order = saveOrder(consumer, delivery);
		saveOrderProduct(request, order, consumer);
	}

	public void refundProduct(Long orderId, Consumer consumer) {
		Order order = orderReadService.findByIdJoinFetch(orderId);
		validateRequest(consumer, order);
		order.getDelivery().updateDeliveryStatus(DeliveryStatus.CANCEL);
		order.updateOrderStatus(OrderStatus.REFUND);
		List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetch(order.getId());

		Long totalAmount = calculateTotalAmount(orderProducts);
		consumer.addBalance(totalAmount);
		consumerRepository.save(consumer);
	}

	private Long calculateTotalAmount(List<OrderProduct> orderProducts) {
		return orderProducts.stream()
			.peek(orderProduct -> orderProduct.getProduct().addStock(orderProduct.getQuantity()))
			.mapToLong(orderProduct -> orderProduct.getAmount() * orderProduct.getQuantity())
			.sum();
	}

	private void validateRequest(Consumer consumer, Order order) {
		if (!Objects.equals(order.getConsumer().getId(), consumer.getId())) {
			throw new BaseException(NO_PERMISSION);
		}
		if (!Objects.equals(order.getDelivery().getDeliveryStatus(), DeliveryStatus.BEFORE_DELIVERY)) {
			throw new BaseException(REFUND_NOT_AVAILABLE);
		}
	}

	private void saveOrderProduct(PurchaseProductRequest request, Order order, Consumer consumer) {
		List<OrderProduct> orderProducts = new ArrayList<>();
		long totalAmount = 0L;
		for (PurchaseProductEntry purchaseProductEntry : request.getPurchaseProducts()) {
			Product product = productReadService.findById(purchaseProductEntry.getProductId());
			orderProducts.add(createOrderProduct(order, purchaseProductEntry, product));
			long amount = purchaseProductEntry.getQuantity() * product.getAmount();
			totalAmount += amount;
			product.decreaseStock(purchaseProductEntry.getQuantity());
		}
		updateConsumerBalance(consumer, totalAmount);
		orderProductJdbcRepository.saveAllBatch(orderProducts);
	}

	private void updateConsumerBalance(Consumer consumer, Long totalAmount) {
		if (consumer.getBalance() < totalAmount) {
			throw new BaseException(NOT_ENOUGH_BALANCE);
		}
		consumer.decreaseBalance(totalAmount);
		consumerRepository.save(consumer);
	}

	private OrderProduct createOrderProduct(Order order, PurchaseProductEntry purchaseProductEntry, Product product) {
		return OrderProduct.builder()
			.quantity(purchaseProductEntry.getQuantity())
			.amount(product.getAmount())
			.order(order)
			.product(product)
			.build();
	}

	private Order saveOrder(Consumer consumer, Delivery delivery) {
		return orderRepository.save(Order.builder()
			.orderStatus(OrderStatus.COMPLETE_PAYMENT)
			.consumer(consumer)
			.delivery(delivery)
			.build());
	}

	private Delivery saveDelivery(Consumer consumer) {
		return deliveryRepository.save(Delivery.builder()
			.address(consumer.getAddress())
			.invoiceNumber(createInvoiceNumber(consumer))
			.build());
	}

	private void saveConsumer(SignUpRequest request) {
		consumerRepository.save(Consumer.builder()
			.email(request.getEmail())
			.address(request.getAddress())
			.build());
	}

	private String createInvoiceNumber(Consumer consumer) {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern(INVOICE_NUMBER_PATTERN)) + consumer.getId();
	}
}
