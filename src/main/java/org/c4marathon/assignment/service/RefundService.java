package org.c4marathon.assignment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.OrderStatus;
import org.c4marathon.assignment.domain.Payment;
import org.c4marathon.assignment.domain.Refund;
import org.c4marathon.assignment.domain.RefundStatus;
import org.c4marathon.assignment.domain.ShipmentStatus;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundService {

	private final MemberService memberService;

	private final RefundRepository refundRepository;

	private final OrderService orderService;

	private final MonetaryTransactionService monetaryTransactionService;


	@Transactional
	// 소비자는 특정 (복합)주문건에 대해 반품 요청을 진행한다
	public Refund refund(Long memberId, Long orderId) {
		Order order = orderService.findById(orderId);
		Member customer = memberService.findCustomerById(memberId);
		List<OrderItem> orderItems = order.getOrderItems();

		if(order.getOrderStatus() == OrderStatus.ORDERED_SHIPPED){
			throw ErrorCd.INVALID_ARGUMENT.serviceException("배송이 이미 시작되어 반품이 어렵습니다");
		}

		// 1. 고객의 지출 금액만큼 다시 충전하고 기업의 매입의 환불을 새로 기록합니다.
		Payment payment = monetaryTransactionService.transactionsForRefunding(orderItems, customer);

		// 2. 주문한 제품들의 재고를 다시 복구합니다.
		addStockQuantity(orderItems);

		// 3. 주문한 상품에 대해 Refund(반품 메타데이터) 엔티티를 생성합니다.
		return refundOrder(order, payment);
	}

	/*
	반품 요청시 반품 사항에 대한 메타데이터를 별도로 생성한다.
	일반적인 쇼핑몰의 경우,
	반품기록을 포함한 모든 판매데이터는 유지되며
	삭제되지 않기 때문에 Delete를 구현하지 않았다.
	*/
	private Refund refundOrder(Order order, Payment payment) {
		// 1. Order의 상태를 사용자가 요청한 반품 대기로 변경합니다.
		order.setOrderStatus(OrderStatus.REFUND_REQUESTED_BY_CUSTOMER);

		// 2. 반송 요청에 해당하는 배송 요청을 새로 생성하도록, 상태를 변경합니다.
		order.setShipmentStatus(ShipmentStatus.REFUND_PENDING);

		// 3. 이미 반품 요청->대기중인 상품은 다시 반품 요청을 할 수 없습니다.
		order.setRefundable(false);

		Refund refund = new Refund();
		refund.setRefundStatus(RefundStatus.PENDING);
		refund.setOrder(order);
		refund.setRefundRequestedDate(LocalDateTime.now());
		refund.setPayment(payment);
		refund.setSeller(order.getSeller());

		// 4. 반품 건 엔티티 생성.
		return refundRepository.save(refund);
	}

	@Transactional(readOnly = true)
	public Refund findById(Long refundId) {
		Optional<Refund> optionalRefund = refundRepository.findById(refundId);

		if (optionalRefund.isEmpty()) {
			throw ErrorCd.NO_SUCH_ITEM.serviceException("반품 건을 조회할 수 없습니다");
		}

		return optionalRefund.get();
	}

	@Transactional(readOnly = true)
	public List<Refund> findBySeller(Member seller) {
		List<Refund> refundBySeller = refundRepository.findRefundBySeller(seller);

		if (refundBySeller.isEmpty()) {
			throw ErrorCd.NO_SUCH_ITEM.serviceException("판매자에게서 조회되는 반품건이 없습니다.");
		}
		return refundBySeller;
	}

	// 제품 재고를 orderItem에 기재된 count로 기준으로 증가시킴(반품).
	private void addStockQuantity(List<OrderItem> orderItems) {
		for (OrderItem orderItem : orderItems) {
			Item item = orderItem.getItem();
			item.addStock(orderItem.getCount());
		}
	}
}
