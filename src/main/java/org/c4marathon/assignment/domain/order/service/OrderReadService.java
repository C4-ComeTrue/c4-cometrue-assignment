package org.c4marathon.assignment.domain.order.service;

import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.repository.OrderRepository;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderReadService {

	private final OrderRepository orderRepository;

	/**
	 * Consumer와 Delivery와 조인한 Order를 조회
	 */
	@Transactional(readOnly = true)
	public Order findById(Long id) {
		return orderRepository.findById(id)
			.orElseThrow(() -> ErrorCode.ORDER_NOT_FOUND.baseException("id: %d", id));
	}
}
