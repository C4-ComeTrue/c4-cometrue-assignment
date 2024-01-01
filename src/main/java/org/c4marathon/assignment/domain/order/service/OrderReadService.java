package org.c4marathon.assignment.domain.order.service;

import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.order.repository.OrderRepository;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OrderReadService {

	private final OrderRepository orderRepository;

	public Order findByIdJoinFetch(Long id) {
		return orderRepository.findByIdJoinFetch(id)
			.orElseThrow(() -> new BaseException(ErrorCode.ORDER_NOT_FOUND));
	}
}
