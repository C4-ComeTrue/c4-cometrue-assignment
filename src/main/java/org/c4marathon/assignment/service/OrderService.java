package org.c4marathon.assignment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.OrderStatus;
import org.c4marathon.assignment.domain.Payment;
import org.c4marathon.assignment.domain.ShipmentStatus;
import org.c4marathon.assignment.domain.CartItem;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final MemberService memberService;
	private final MonetaryTransactionService monetaryTransactionService;
	private final OrderItemService orderItemService;

	private final OrderRepository orderRepository;

	// 복합 주문(Order)의 기본키를 이용하여 특정 복합 주문 건을 조회한다.
	public Order findById(Long orderId) {
		Optional<Order> order = orderRepository.findById(orderId);
		if (order.isEmpty()) {
			throw ErrorCd.NO_SUCH_ITEM.serviceException("주문을 찾을 수 없습니다");
		}
		return order.get();
	}

	@Transactional
	// 소비자는 장바구니 내에 있는 주문에 대해 일괄주문을 진행한다.
	public Order proceed(List<CartItem> cartItems, Long customerId, Long sellerId) {
		Member customer = memberService.findCustomerById(customerId);
		Member seller = memberService.findSellerById(sellerId);

		// 장바구니 내 상품이 없는 경우 결제를 진행할 수 없습니다.
		if (cartItems.isEmpty()) {
			throw ErrorCd.INVALID_ARGUMENT.serviceException("장바구니가 비어있습니다");
		}

		List<OrderItem> itemList = orderItemService.createOrderItems(cartItems);

		// 1. 제품 재고를 감소시킵니다.
		reducingStockQuantity(itemList);

		// 2. 고객의 지출을 기록, 기업의 매입을 기록합니다.
		Payment payment = monetaryTransactionService.transactionsForSelling(itemList, customer);

		// 3. 주문을 생성합니다.
		return orderRepository.save(createOrder(itemList, customer, seller, payment));
	}

	@Transactional
	public void requestRefund(Long orderId, Long customerId){
		Member customer = memberService.findById(customerId);
		Order order = findById(orderId);

		if (order.getCustomer() != customer) {
			throw ErrorCd.NO_PERMISSION.serviceException("다른 사용자가 구입한 요청에 대한 반품 요청입니다.");
		}

		if (order.getOrderStatus() != OrderStatus.ORDERED_PENDING) {
			throw ErrorCd.INVALID_ARGUMENT.serviceException("배송 대기중인 상태에서만 반품 신청이 가능합니다.");
		}

		order.setOrderStatus(OrderStatus.REFUND_REQUESTED_BY_CUSTOMER);
		order.setRefundable(false);
		order.setShipmentStatus(ShipmentStatus.REFUND_PENDING);
	}


	@Transactional
	// 소비자는 배송이 출발한 주문건에 대해 수취확인(구매확정)을 한다.
	public void orderConfirmation(Long memberId, Long orderId) {
		memberService.findCustomerById(memberId);
		Order order = findById(orderId);
		List<OrderItem> orderItems = order.getOrderItems();

		for (OrderItem orderItem : orderItems) {
			// 1. 주문 내에 있는 아이템들에 대해 순차적으로 수수료 지급 절차 진행.
			monetaryTransactionService.commissionProcedure(orderItem, orderItem.getItem().getSeller());
		}

		// 2. 지급 절차가 끝나면 제품 상태를 CUSTOMER_ACCEPTED(구매 확정)으로 변경함.
		order.setOrderStatus(OrderStatus.CUSTOMER_ACCEPTED);
	}

	private Order createOrder(List<OrderItem> itemList, Member customer,  Member seller, Payment payment) {
		Order order = new Order();
		order.setCustomer(customer);
		order.setSeller(seller);
		order.setOrderItems(itemList);
		order.setPayment(payment);
		order.setOrderDate(LocalDateTime.now());
		// order.setShipment() 송장 생성 전까지는 -> nullable.
		order.setShipmentStatus(ShipmentStatus.PENDING);
		order.setOrderStatus(OrderStatus.ORDERED_PENDING);
		order.setRefundable(true);
		order.setRefunded(false);
		return order;
	}

	// 제품 재고를 orderItem에 기재된 count를 기준으로 감소시킴(구매).
	private void reducingStockQuantity(List<OrderItem> orderItems) {
		for (OrderItem orderItem : orderItems) {
			Item item = orderItem.getItem();
			item.removeStock(orderItem.getCount());
		}
	}

}
