package org.c4marathon.assignment.domain.pay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChargePayRequest {

	@NotNull(message = "amount is null")
	@Min(value = 0, message = "amount is less than zero")
	private Long amount;
}
