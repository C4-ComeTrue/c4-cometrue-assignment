package org.c4marathon.assignment.service;

import java.util.List;

import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.OrderStatus;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.OrderItemRepository;
import org.c4marathon.assignment.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SellerService {

	private final OrderItemRepository orderItemRepository; // 주문된 항목에 대해 접근
	private final OrderRepository orderRepository;

	private final OrderService orderService;
	private final MemberService memberService;
	private final ItemService itemService;
	private final ShipmentService shipmentService;


	// 사업장 내 전체 주문 목록 표시
	@Transactional(readOnly = true)
	public List<OrderItem> findOrders(Long itemId, Long memberId) {
		Member seller = sellerTypeValidation(memberId);

		Item item = itemService.findById(itemId);

		if (!item.getSeller().equals(seller)) {
			throw ErrorCd.NO_PERMISSION.serviceException("상품 판매자와 계정이 일치하지 않습니다");
		}

		return orderItemRepository.findOrderItemsByItemSeller(seller);
	}

	// 사업장 내 특정 OrderStatus에 대한 주문 목록을 표시
	@Transactional(readOnly = true)
	public List<Order> findOrdersByOrderStatus(Long memberId, OrderStatus orderStatus) {
		Member seller = sellerTypeValidation(memberId);
		return orderRepository.findOrdersBySellerAndOrderStatus(seller, orderStatus);
	}


	// 판매자는 들어온 특정 주문에 대해 승인.
	@Transactional
	public Order acceptOrder(Long orderId, Long memberId) {
		sellerTypeValidation(memberId);

		Order order = orderService.findById(orderId);
		order.setOrderStatus(OrderStatus.ORDERED_ACCEPTED);
		return order;
	}

	// 특정 주문에 대해 송장번호를 포함하여 배송 발행.
	public void issueShipment(Long orderId, Long memberId, String trackerId) {
		sellerTypeValidation(memberId);
		shipmentService.issueTracker(orderId, trackerId);
	}

	// 특정 주문에 대해 송장번호를 포함하여 배송 완료처리.
	public void shipmentCompleted(Long orderId, Long memberId) {
		sellerTypeValidation(memberId);
		shipmentService.completion(orderId);
	}

	// 접근한 계정이 판매자 계정인지 검증.
	private Member sellerTypeValidation(Long memberId) {
		Member seller = memberService.findSellerById(memberId);

		if (seller.getMemberType() != MemberType.ROLE_SELLER) {
			throw ErrorCd.NO_PERMISSION.serviceException("판매자만 접근할 수 있습니다");
		}
		return seller;
	}




}
