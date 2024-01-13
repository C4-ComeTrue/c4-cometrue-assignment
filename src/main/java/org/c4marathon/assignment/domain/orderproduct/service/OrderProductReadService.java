package org.c4marathon.assignment.domain.orderproduct.service;

import java.util.List;

import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderProductReadService {

	private final OrderProductRepository orderProductRepository;

	@Transactional(readOnly = true)
	public List<OrderProduct> findByOrderJoinFetchProduct(Long orderId) {
		return orderProductRepository.findByOrderJoinFetchProduct(orderId);
	}

	@Transactional(readOnly = true)
	public List<OrderProduct> findByOrderJoinFetchProductAndSeller(Long orderId) {
		return orderProductRepository.findByOrderJoinFetchProductAndSeller(orderId);
	}
}
