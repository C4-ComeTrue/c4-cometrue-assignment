package org.c4marathon.assignment.domain.deliverycompany.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import java.util.Objects;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.service.DeliveryReadService;
import org.c4marathon.assignment.domain.deliverycompany.dto.request.UpdateDeliveryStatusRequest;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.c4marathon.assignment.global.constant.DeliveryStatus;
import org.c4marathon.assignment.global.error.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class DeliveryCompanyService {

	private final DeliveryCompanyRepository deliveryCompanyRepository;
	private final DeliveryCompanyReadService deliveryCompanyReadService;
	private final DeliveryReadService deliveryReadService;

	public void signup(SignUpRequest request) {
		if (deliveryCompanyReadService.existsByEmail(request.getEmail())) {
			throw new BaseException(ALREADY_DELIVERY_COMPANY_EXISTS);
		}

		saveDeliveryCompany(request);
	}

	public void updateDeliveryStatus(
		Long deliveryId,
		UpdateDeliveryStatusRequest request,
		DeliveryCompany deliveryCompany
	) {
		Delivery delivery = deliveryReadService.findByIdJoinFetch(deliveryId);
		if (!Objects.equals(delivery.getDeliveryCompany().getId(), deliveryCompany.getId())) {
			throw new BaseException(NO_PERMISSION);
		}

		validateRequest(request, delivery);
		delivery.updateDeliveryStatus(request.getDeliveryStatus());
	}

	private void validateRequest(UpdateDeliveryStatusRequest request, Delivery delivery) {
		DeliveryStatus future = request.getDeliveryStatus();
		DeliveryStatus current = delivery.getDeliveryStatus();
		if (isInvalidChangeStatus(future, current)) {
			throw new BaseException(INVALID_DELIVERY_STATUS_REQUEST);
		}
	}

	private boolean isInvalidChangeStatus(DeliveryStatus future, DeliveryStatus current) {
		return future.equals(DeliveryStatus.BEFORE_DELIVERY)
			|| (future.equals(DeliveryStatus.IN_DELIVERY) && !current.equals(DeliveryStatus.BEFORE_DELIVERY))
			|| (future.equals(DeliveryStatus.COMPLETE_DELIVERY) && !current.equals(DeliveryStatus.IN_DELIVERY));
	}

	private void saveDeliveryCompany(SignUpRequest request) {
		deliveryCompanyRepository.save(DeliveryCompany.builder()
			.email(request.getEmail())
			.build());
	}
}
