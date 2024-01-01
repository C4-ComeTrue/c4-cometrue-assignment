package org.c4marathon.assignment.domain.orderproduct.service;

import java.util.List;

import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class OrderProductReadService {

	private final OrderProductRepository orderProductRepository;

	public List<OrderProduct> findByOrderJoinFetch(Long orderId) {
		return orderProductRepository.findByOrderJoinFetch(orderId);
	}
}
