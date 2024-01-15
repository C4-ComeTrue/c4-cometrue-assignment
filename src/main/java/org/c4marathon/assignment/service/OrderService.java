package org.c4marathon.assignment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.ChargeType;
import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.OrderStatus;
import org.c4marathon.assignment.domain.Payment;
import org.c4marathon.assignment.domain.Refund;
import org.c4marathon.assignment.domain.RefundStatus;
import org.c4marathon.assignment.domain.ShipmentStatus;
import org.c4marathon.assignment.domain.ShoppingCart;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.MemberRepository;
import org.c4marathon.assignment.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

	private final MemberService memberService;
	private final PaymentService paymentService;
	private final SalesService salesService;
	private final ShipmentService shipmentService;
	private final RefundService refundService;

	private final OrderRepository orderRepository;
	private final MemberRepository memberRepository;

	public Order findById(Long orderId){
		Optional<Order> order = orderRepository.findById(orderId);
		if (order.isEmpty()) {
			throw ErrorCd.NO_SUCH_ITEM.serviceException("주문을 찾을 수 없습니다");
		}
		return order.get();
	}

	public void proceed(Long customerId, Long sellerId) {
		Member customer = memberService.findCustomerId(customerId);
		Member seller = memberService.findSellerId(sellerId);

		// 주문은 장바구니 내에서만 가능합니다.
		ShoppingCart shoppingCart = customer.getShoppingCart();
		List<OrderItem> itemList = shoppingCart.getItemList();

		// 장바구니 내 상품이 없는 경우 결제를 진행할 수 없습니다.
		if (itemList.isEmpty()) {
			throw ErrorCd.INVALID_ARGUMENT.serviceException("장바구니가 비어있습니다");
		}

		// 1. 제품 재고를 감소시킵니다.
		reducingStockQuantity(itemList);

		// 2. 고객의 지출을 기록, 기업의 매입을 기록합니다.
		Payment payment = monetaryTransactionsForSelling(itemList, customer);

		// 3. 주문을 생성합니다.
		createOrder(itemList, customer, seller, payment);
	}

	public void refund(Long memberId, Long orderId){
		Order order = findById(orderId);
		Member customer = memberService.findCustomerId(memberId);
		List<OrderItem> orderItems = order.getOrderItems();

		// 1. 고객의 지출 금액만큼 다시 충전하고 기업의 매입의 환불을 새로 기록합니다.
		monetaryTransactionsForRefunding(orderItems, customer);

		// 2. 주문한 제품들의 재고를 다시 복구합니다.
		addStockQuantity(orderItems);

		// 3. 주문한 상품에 대해 Refund(반품 메타데이터) 엔티티를 생성합니다.
		Refund refund = refundOrder(order);
	}


	public void orderConfirmation(Long memberId, Long orderId){
		Member customer = memberService.findCustomerId(memberId);
		Order order = findById(orderId);
		List<OrderItem> orderItems = order.getOrderItems();

		for (OrderItem orderItem : orderItems) {
			// 1. 주문 내에 있는 아이템들에 대해 순차적으로 수수료 지급 절차 진행.
			salesService.commissionProcedure(orderId, orderItem.getOrderItemPk(),
				customer, orderItem.getItem().getSeller());
		}
		order.setOrderStatus(OrderStatus.CUSTOMER_ACCEPTED);
	}

	private void createOrder(List<OrderItem> itemList, Member customer,  Member seller, Payment payment) {
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
		orderRepository.save(order);
	}


	// Order 상태 변경 및 반품 엔티티 생성
	private Refund refundOrder(Order order) {
		// 1. Order의 상태를 사용자가 요청한 반품 대기로 변경합니다.
		order.setOrderStatus(OrderStatus.REFUND_REQUESTED_BY_CUSTOMER);

		// 2. 반송 요청에 해당하는 배송 요청을 새로 생성하도록, 상태를 변경합니다.
		order.setShipmentStatus(ShipmentStatus.REFUND_PENDING);

		// 3. 이미 반품 요청->대기중인 상품은 다시 반품 요청을 할 수 없습니다.
		order.setRefundable(false);

		// 4. 반품 건 엔티티 생성.
		Refund refund = new Refund();
		refund.setOrder(order);
		refund.setRefundStatus(RefundStatus.PENDING);
		refund.setCustomer(order.getCustomer());
		refund.setSeller(order.getSeller());
		refund.setRefundRequestedDate(LocalDateTime.now());
		return refundService.save(refund);
	}

	// 제품 재고를 orderItem에 기재된 count를 기준으로 감소시킴(구매).
	private void reducingStockQuantity(List<OrderItem> orderItems) {
		for (OrderItem orderItem : orderItems) {
			Item item = orderItem.getItem();
			item.removeStock(orderItem.getCount());
		}
	}

	// 제품 재고를 orderItem에 기재된 count로 기준으로 증가시킴(반품).
	private void addStockQuantity(List<OrderItem> orderItems) {
		for (OrderItem orderItem : orderItems) {
			Item item = orderItem.getItem();
			item.addStock(orderItem.getCount());
		}
	}

	// 제품 판매시 금액 이동
	private Payment monetaryTransactionsForSelling(List<OrderItem> orderItems, Member customer) {
		int itemsTotalPrice = 0;

		// FinancialAdmin은 단 하나만 존재하며 모든 구매 반품 흐름은 이 admin을 통해 확인함.
		Member admin = memberRepository.findFinancialAdmin();

		for (OrderItem orderItem : orderItems) {
			itemsTotalPrice += orderItem.getTotalPrice();
			// 기업 잔액 변동 적용 (매출) - 제품별로 구매를 각각 기록함.
			salesService.addSales(orderItem, customer, admin, orderItem.getTotalPrice(), ChargeType.CHARGE);
		}

		// 고객의 잔고에서 구매합계 금액을 출금처리함.
		return paymentService.discharge(itemsTotalPrice, customer);
	}

	// 제품 반품시 금액 이동, 이전 메서드와 차감 -> 충전을 제외하고 로직 동일.
	private Payment monetaryTransactionsForRefunding(List<OrderItem> orderItems, Member customer) {
		int itemsTotalPrice = 0;
		Member admin = memberRepository.findFinancialAdmin();

		for (OrderItem orderItem : orderItems) {
			itemsTotalPrice += orderItem.getTotalPrice();
			// 기업 잔액 변동 적용 (매출)
			salesService.addSales(orderItem, admin, customer, orderItem.getTotalPrice(), ChargeType.REFUND);
		}

		return paymentService.charge(itemsTotalPrice, customer);
	}
}
