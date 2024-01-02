package org.c4marathon.assignment.domain.delivery.service;

import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.repository.DeliveryRepository;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DeliveryReadService {

	private final DeliveryRepository deliveryRepository;

	public Delivery findByIdJoinFetch(Long id) {
		return deliveryRepository.findByIdJoinFetch(id)
			.orElseThrow(() -> new BaseException(ErrorCode.DELIVERY_NOT_FOUND));
	}
}
