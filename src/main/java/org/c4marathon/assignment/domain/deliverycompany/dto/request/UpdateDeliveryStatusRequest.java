package org.c4marathon.assignment.domain.deliverycompany.dto.request;

import org.c4marathon.assignment.global.constant.DeliveryStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequest {

	@NotNull(message = "deliveryStatus is null")
	private DeliveryStatus deliveryStatus;
}
