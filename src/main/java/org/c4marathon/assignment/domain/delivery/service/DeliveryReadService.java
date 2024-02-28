package org.c4marathon.assignment.domain.delivery.service;

import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.repository.DeliveryRepository;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DeliveryReadService {

	private final DeliveryRepository deliveryRepository;

	/**
	 * id로 DeliveryCompany와 조인한 Delivery 조회
	 */
	@Transactional(readOnly = true)
	public Delivery findByIdJoinFetch(Long id) {
		return deliveryRepository.findByIdJoinFetch(id)
			.orElseThrow(() -> ErrorCode.DELIVERY_NOT_FOUND.baseException("id: %d", id));
	}
}
