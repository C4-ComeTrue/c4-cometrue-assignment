package org.c4marathon.assignment.domain.consumer.service;

import static org.c4marathon.assignment.global.constant.CouponType.*;
import static org.c4marathon.assignment.global.constant.DeliveryStatus.*;
import static org.c4marathon.assignment.global.constant.OrderStatus.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.ObjLongConsumer;

import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductEntry;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.c4marathon.assignment.domain.coupon.service.CouponReadService;
import org.c4marathon.assignment.domain.coupon.service.CouponRetryService;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.repository.DeliveryRepository;
import org.c4marathon.assignment.domain.delivery.service.DeliveryReadService;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.discountpolicy.entity.DiscountPolicy;
import org.c4marathon.assignment.domain.discountpolicy.service.DiscountPolicyReadService;
import org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCoupon;
import org.c4marathon.assignment.domain.issuedcoupon.service.CouponRestrictionManager;
import org.c4marathon.assignment.domain.issuedcoupon.service.IssuedCouponReadService;
import org.c4marathon.assignment.domain.issuedcoupon.service.LockedCouponService;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.repository.OrderRepository;
import org.c4marathon.assignment.domain.order.service.OrderReadService;
import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductJdbcRepository;
import org.c4marathon.assignment.domain.orderproduct.service.OrderProductReadService;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.pointlog.repository.PointLogRepository;
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
	public static final double POINT_RATE = 0.025;

	private final ConsumerRepository consumerRepository;
	private final OrderRepository orderRepository;
	private final DeliveryRepository deliveryRepository;
	private final ProductReadService productReadService;
	private final OrderProductJdbcRepository orderProductJdbcRepository;
	private final OrderReadService orderReadService;
	private final OrderProductReadService orderProductReadService;
	private final DeliveryCompanyReadService deliveryCompanyReadService;
	private final PointLogRepository pointLogRepository;
	private final DeliveryReadService deliveryReadService;
	private final IssuedCouponReadService issuedCouponReadService;
	private final CouponReadService couponReadService;
	private final LockedCouponService lockedCouponService;
	private final DiscountPolicyReadService discountPolicyReadService;
	private final CouponRestrictionManager couponRestrictionManager;
	private final CouponRetryService couponRetryService;

	/**
	 * 상품 구매
	 * 최종 결제 금액 = 총 구입 금액 - 사용할 포인트
	 * 이후 구매 확정 단계에서 사용하기 위해 Order Entity에 포인트 관련 필드를 추가
	 * 선착순 사용 쿠폰일 경우에, 주문을 하고 환불을 해도 쿠폰은 환불되지 않음.
	 * 포인트 같은 경우에는 모든 할인(총 주문 금액 - 쿠폰 할인 - 포인트 사용 금액)이 적용된 최종 결제 금액에 5퍼센트가 적용됨.
	 * @param consumer 상품 구매하는 소비자
	 */
	@Transactional
	public void purchaseProduct(PurchaseProductRequest request, Consumer consumer) {
		IssuedCoupon issuedCoupon = getIssuedCoupon(request.issuedCouponId(), consumer);
		Coupon coupon = useCoupon(issuedCoupon);

		try {
			decreasePoint(consumer, request.point());
			Order order = saveOrder(consumer, request.point());
			List<OrderProduct> orderProducts = getOrderProducts(request, order);
			long totalAmount = calculateTotalAmount(orderProducts, coupon);
			decreaseBalance(consumer, totalAmount - request.point());
			saveOrderInfo(request, consumer, order, orderProducts, totalAmount, issuedCoupon);
		} catch (Exception e) {
			// 예외가 발생했을때 선착순 사용 쿠폰을 원상복구 해야함
			couponRetryService.decreaseUsedCount(issuedCoupon, coupon);
			throw e;
		}
	}

	/**
	 * 상품 환불
	 * OrderStatus의 상태가 변경되면 transaction commit event를 발생해
	 * 이후 해당 event에서 사용한 포인트, 결제 금액 환불 진행
	 * 또한, 이벤트 도중 예기치 못한 에러가 발생 시에도 복구가능해야 하므로 PointLog 엔티티를 저장
	 * @param orderId 환불하려는 Order PK
	 * @param consumer 환불하려는 소비자
	 */
	@Transactional
	public void refundOrder(Long orderId, Consumer consumer) {
		Order order = orderReadService.findById(orderId);
		Delivery delivery = deliveryReadService.findByIdJoinFetch(order.getDeliveryId());
		validateRefundRequest(consumer, order, delivery);

		updateStatusWhenRefund(order, delivery);
		refundCoupon(order);
		savePointLog(consumer, order, false);
	}

	/**
	 * 상품 구매 확정
	 * 구매 확정 시에 포인트를 지급
	 * 구매 확정 이전에 포인트를 지급하게 될 경우,
	 * 물건 구매 -> 포인트 증가 -> 해당 포인트로 다른 물건 구매 -> 이전 물건 환불
	 * 같은 case가 가능해 포인트가 증식됨
	 * 따라서 event를 발생시켜 해당 event에서 구매자의 포인트 적립을 수행
	 * 또한, 이벤트 도중 예기치 못한 에러가 발생 시에도 복구가능해야 하므로 PointLog 엔티티를 저장
	 * @param orderId 구매 확정하려는 Order PK
	 * @param consumer 구매 확정하려는 소비자
	 */
	@Transactional
	public void confirmOrder(Long orderId, Consumer consumer) {
		Order order = orderReadService.findById(orderId);
		Delivery delivery = deliveryReadService.findByIdJoinFetch(order.getDeliveryId());
		validateConfirmRequest(consumer, order, delivery);
		order.updateOrderStatus(CONFIRM);
		List<OrderProduct> orderProducts = orderProductReadService.findByOrderJoinFetchProductAndSeller(orderId);
		addSellerBalance(orderProducts);
		addOrderCount(orderProducts);

		savePointLog(consumer, order, true);
	}

	private IssuedCoupon getIssuedCoupon(Long issuedCouponId, Consumer consumer) {
		if (issuedCouponId == null) {
			return null;
		}
		IssuedCoupon issuedCoupon = issuedCouponReadService.findById(issuedCouponId);
		issuedCoupon.validatePermission(consumer.getId());
		return issuedCoupon;
	}

	/**
	 * 쿠폰 사용
	 * 내가 발급받은 쿠폰이 아니면 exception
	 * 기간이 지난 쿠폰이면 exception
	 * 중복 불가능 쿠폰인데 이미 사용됐으면 exception
	 * 선착순 발급 쿠폰인 경우는 사용만 하면 되기 때문에 따로 락 메커니즘은 필요없음, 중복 사용도 가능함
	 * 선착순 사용 쿠폰인 경우는 락 메커니즘이 필요함.
	 * 그래서 여기도 사용 횟수 다다르면 couponRestrictionManager에 캐싱해둠
	 */
	private Coupon useCoupon(IssuedCoupon issuedCoupon) {
		if (issuedCoupon == null) {
			return null;
		}
		Coupon coupon = couponReadService.findById(issuedCoupon.getCouponId());
		coupon.validateTime();
		validateRedundant(coupon, issuedCoupon);
		if (coupon.getCouponType() == USE_COUPON) {
			couponRestrictionManager.validateCouponUsable(coupon.getId());
			lockedCouponService.increaseUsedCount(coupon.getId(), issuedCoupon.getId());
		} else {
			issuedCoupon.increaseUsedCount();
		}
		return coupon;
	}

	private void validateRedundant(Coupon coupon, IssuedCoupon issuedCoupon) {
		if (!coupon.getRedundantUsable() && issuedCoupon.getUsedCount() > 0) {
			couponRestrictionManager.addNotUsableCoupon(coupon.getId());
			throw ALREADY_USED_COUPON.baseException();
		}
	}

	private long calculateTotalAmount(List<OrderProduct> orderProducts, Coupon coupon) {
		long totalAmount = orderProducts.stream()
			.mapToLong(OrderProduct::getAmount)
			.sum();
		if (coupon != null) {
			DiscountPolicy discountPolicy = discountPolicyReadService.findById(coupon.getDiscountPolicyId());
			totalAmount = Math.max(0, totalAmount - discountPolicy.calculateDiscountAmount(totalAmount));
		}
		return totalAmount;
	}

	/**
	 * 환불은 선착순 발급 쿠폰만 가능하기 때문에 동시성 제어를 딱히 할 필요가 없음
	 */
	private void refundCoupon(Order order) {
		if (order.getIssuedCouponId() != null) {
			IssuedCoupon issuedCoupon = issuedCouponReadService.findById(order.getIssuedCouponId());
			Coupon coupon = couponReadService.findById(issuedCoupon.getCouponId());
			if (coupon.getCouponType() != USE_COUPON) {
				issuedCoupon.decreaseUsedCount();
			}
		}
	}

	/**
	 * 주문 시 product에 대한 구매 횟수를 증가함
	 */
	private void addOrderCount(List<OrderProduct> orderProducts) {
		orderProducts.forEach(orderProduct -> orderProduct.getProduct().increaseOrderCount());
	}

	private void saveOrderInfo(PurchaseProductRequest request, Consumer consumer, Order order,
		List<OrderProduct> orderProducts, long totalAmount, IssuedCoupon issuedCoupon) {
		orderProductJdbcRepository.saveAllBatch(orderProducts);
		order.updateEarnedPoint(getPurchasePoint(totalAmount - request.point()));
		order.updateTotalAmount(totalAmount);
		order.updateDeliveryId(saveDelivery(consumer).getId());
		order.updateIssuedCouponId(issuedCoupon == null ? null : issuedCoupon.getId());
	}

	/**
	 * Product의 stock 감소
	 * Product에 Pessimistic lock을 걸어 다중 스레드 환경에서도 stock을 안전하게 관리
	 * Order에 대한 OrderProduct를 저장
	 * @return orderProducts
	 */
	private List<OrderProduct> getOrderProducts(PurchaseProductRequest request, Order order) {
		return request.purchaseProducts().stream()
			.map(purchaseProductEntry -> {
				Product product = productReadService.findById(purchaseProductEntry.productId());
				decreaseStock(purchaseProductEntry, product);
				return createOrderProduct(order, purchaseProductEntry, product);
			})
			.toList();
	}

	/**
	 * 소비자의 잔고 또는 포인트 변경
	 * @param amount 감소 또는 증가할 금액
	 * @param updater 감소 또는 증가 작업을 결정 하는 interface
	 */
	private void updateConsumer(Consumer consumer, long amount, ObjLongConsumer<Consumer> updater) {
		updater.accept(consumer, amount);
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
	private void updateStatusWhenRefund(Order order, Delivery delivery) {
		delivery.updateDeliveryStatus(COMPLETE_DELIVERY);
		order.updateOrderStatus(REFUND);
	}

	private long getPurchasePoint(long totalAmount) {
		return (long)(totalAmount * POINT_RATE);
	}

	/**
	 * 구매 확정 시 검증 과정
	 * 1. 주문한 소비자가 현재 로그인한 소비자인지
	 * 2. 올바른 주문, 배송 상태인지
	 */
	private void validateConfirmRequest(Consumer consumer, Order order, Delivery delivery) {
		if (!order.getConsumer().getId().equals(consumer.getId())) {
			throw NO_PERMISSION.baseException();
		}
		if (!order.getOrderStatus().isPayedUp() || !delivery.getDeliveryStatus().isDelivered()) {
			throw CONFIRM_NOT_AVAILABLE.baseException("current status: %s", order.getOrderStatus());
		}
	}

	/**
	 * 환불 시 검증 과정
	 * 1. 주문한 소비자가 현재 로그인한 소비자인지
	 * 2. 올바른 배송 상태인지
	 */
	private void validateRefundRequest(Consumer consumer, Order order, Delivery delivery) {
		if (!order.getConsumer().getId().equals(consumer.getId())) {
			throw NO_PERMISSION.baseException();
		}
		if (!delivery.getDeliveryStatus().isPending()) {
			throw REFUND_NOT_AVAILABLE.baseException("delivery status: %s",
				delivery.getDeliveryStatus().toString());
		}
	}

	/**
	 * 재고가 부족할 시 예외를 반환하고, 아니면 재고를 감소
	 */
	private void decreaseStock(PurchaseProductEntry purchaseProductEntry, Product product) {
		if (product.isSoldOut() || product.getStock() < purchaseProductEntry.quantity()) {
			throw NOT_ENOUGH_PRODUCT_STOCK.baseException(
				"current stock: %d, request stock: %d", product.getStock(), purchaseProductEntry.quantity());
		}
		product.decreaseStock(purchaseProductEntry.quantity());
	}

	/**
	 * 잔고가 부족할 시 예외를 반환하고, 아니면 잔고를 감소
	 */
	private void decreaseBalance(Consumer consumer, long totalAmount) {
		if (totalAmount < 0) {
			throw EXCESSIVE_POINT_USE.baseException();
		}
		if (consumer.getBalance() < totalAmount) {
			throw NOT_ENOUGH_BALANCE.baseException("total amount: %d", totalAmount);
		}
		updateConsumer(consumer, totalAmount, Consumer::decreaseBalance);
	}

	/**
	 * 포인트가 부족할 시 예외를 반환하고, 아니면 포인트를 감소
	 */
	private void decreasePoint(Consumer consumer, long point) {
		if (consumer.getPoint() < point) {
			throw NOT_ENOUGH_POINT.baseException("current point: %d, request point: %d", consumer.getPoint(), point);
		}
		updateConsumer(consumer, -point, Consumer::updatePoint);
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
	 * 송장번호 생성
	 * 현재 시간(nano second) + consumer PK
	 */
	private String createInvoiceNumber(Consumer consumer) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(INVOICE_NUMBER_PATTERN);
		return now.format(formatter) + consumer.getId();
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
	private Order saveOrder(Consumer consumer, long usedPoint) {
		return orderRepository.save(Order.builder()
			.orderStatus(COMPLETE_PAYMENT)
			.consumer(consumer)
			.usedPoint(usedPoint)
			.build());
	}

	private void savePointLog(Consumer consumer, Order order, Boolean isConfirm) {
		pointLogRepository.save(
			PointLog.builder()
				.consumerId(consumer.getId())
				.usedPoint(order.getUsedPoint())
				.earnedPoint(order.getEarnedPoint())
				.totalAmount(order.getTotalAmount())
				.isConfirm(isConfirm)
				.build());
	}
}
