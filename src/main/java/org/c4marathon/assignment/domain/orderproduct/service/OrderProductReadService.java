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

	/**
	 * Product와 조인한 OrderProduct list를 orderId로 조회
	 */
	@Transactional(readOnly = true)
	public List<OrderProduct> findByOrderJoinFetchProduct(Long orderId) {
		return orderProductRepository.findByOrderJoinFetchProduct(orderId);
	}

	/**
	 * Product와 Seller와 조인한 OrderProduct list를 orderId로 조회
	 */
	@Transactional(readOnly = true)
	public List<OrderProduct> findByOrderJoinFetchProductAndSeller(Long orderId) {
		return orderProductRepository.findByOrderJoinFetchProductAndSeller(orderId);
	}

	/**
	 * orderId에 해당하는 OrderProduct list의 amount의 합을 반환
	 */
	@Transactional(readOnly = true)
	public Long findTotalAmountByOrderId(Long orderId) {
		return orderProductRepository.findTotalAmountByOrderId(orderId);
	}
}
