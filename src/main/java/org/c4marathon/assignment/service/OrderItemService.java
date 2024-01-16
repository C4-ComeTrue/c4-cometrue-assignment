package org.c4marathon.assignment.service;

import java.util.Optional;

import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemService {

	private final OrderItemRepository orderItemRepository;

	@Transactional(readOnly = true)
	// 제품별 주문 건을 orderItem의 기본키를 이용하여 조회한다.
	public OrderItem findOrderItemById(Long orderItemId) {
		Optional<OrderItem> optionalOrderItem = orderItemRepository.findById(orderItemId);
		if (optionalOrderItem.isEmpty()) {
			throw ErrorCd.NO_SUCH_ITEM.serviceException("주문 내역이 없습니다");
		}

		return optionalOrderItem.get();
	}

}
