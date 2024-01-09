package org.c4marathon.assignment.domain.deliverycompany.dto.request;

import org.c4marathon.assignment.global.constant.DeliveryStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryStatusRequest(
	@NotNull(message = "deliveryStatus is null")
	DeliveryStatus deliveryStatus
) {
}
