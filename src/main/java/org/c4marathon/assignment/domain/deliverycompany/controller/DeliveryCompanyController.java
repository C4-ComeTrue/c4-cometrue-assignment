package org.c4marathon.assignment.domain.deliverycompany.controller;

import org.c4marathon.assignment.domain.deliverycompany.dto.request.UpdateDeliveryStatusRequest;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyService;
import org.c4marathon.assignment.global.auth.DeliveryCompanyThreadLocal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/deliveries")
public class DeliveryCompanyController {

	private final DeliveryCompanyService deliveryCompanyService;

	@PatchMapping("/{delivery_id}/status")
	public void updateDeliveryStatus(
		@PathVariable("delivery_id") Long deliveryId,
		@RequestBody @Valid UpdateDeliveryStatusRequest request
	) {
		deliveryCompanyService.updateDeliveryStatus(deliveryId, request, DeliveryCompanyThreadLocal.get());
	}
}
