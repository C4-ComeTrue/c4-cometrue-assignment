package org.c4marathon.assignment.domain.deliverycompany.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.service.DeliveryReadService;
import org.c4marathon.assignment.domain.deliverycompany.dto.request.UpdateDeliveryStatusRequest;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.global.constant.DeliveryStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DeliveryCompanyService {

	private final DeliveryReadService deliveryReadService;

	/**
	 * 배송 상태 변경
	 */
	@Transactional
	public void updateDeliveryStatus(
		Long deliveryId,
		UpdateDeliveryStatusRequest request,
		DeliveryCompany deliveryCompany
	) {
		Delivery delivery = deliveryReadService.findByIdJoinFetch(deliveryId);
		if (!delivery.getDeliveryCompany().getId().equals(deliveryCompany.getId())) {
			throw NO_PERMISSION.baseException();
		}

		validateRequest(request, delivery);
		delivery.updateDeliveryStatus(request.deliveryStatus());
	}

	/**
	 * current: 현재 배송 상태
	 * future: 변경할 배송 상태
	 */
	private void validateRequest(UpdateDeliveryStatusRequest request, Delivery delivery) {
		DeliveryStatus future = request.deliveryStatus();
		DeliveryStatus current = delivery.getDeliveryStatus();
		if (isInvalidChangeStatus(future, current)) {
			throw INVALID_DELIVERY_STATUS_REQUEST.baseException("current status: %s, future status: %s",
				delivery.getDeliveryStatus().toString(), request.deliveryStatus().toString());
		}
	}

	/**
	 * 변경할 상태가 BEFORE_DELIVERY 이거나, 상태를 두 단계 이상 건너뛰어 변경하려 한다면 실패
	 */
	private boolean isInvalidChangeStatus(DeliveryStatus future, DeliveryStatus current) {
		return future.isPending()
			|| (future.isDelivering() && !current.isPending())
			|| (future.isDelivered() && !current.isDelivering());
	}
}
