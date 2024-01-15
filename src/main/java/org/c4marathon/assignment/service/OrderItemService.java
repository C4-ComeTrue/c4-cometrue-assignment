package org.c4marathon.assignment.service;

import java.util.Optional;

import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.OrderStatus;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderItemService {

	private final OrderItemRepository orderItemRepository;

	public OrderItem findOrderItemById(Long orderItemId) {
		Optional<OrderItem> optionalOrderItem = orderItemRepository.findById(orderItemId);
		if(optionalOrderItem.isEmpty()){
			throw ErrorCd.NO_SUCH_ITEM.serviceException("주문 내역이 없습니다");
		}

		return optionalOrderItem.get();
	}

	private void orderStatusValidation(Order order, OrderStatus orderStatus) {
		OrderStatus currentStatus = order.getOrderStatus();
		if(currentStatus != OrderStatus.ORDERED_PENDING
			&& orderStatus == OrderStatus.REFUND_REQUESTED_BY_CUSTOMER) {
			throw ErrorCd.INVALID_ARGUMENT.serviceException("이미 배송된 상품은 환불이 어렵습니다");
		}
	}
}
