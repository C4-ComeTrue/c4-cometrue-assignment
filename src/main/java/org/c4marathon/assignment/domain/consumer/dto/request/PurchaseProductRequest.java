package org.c4marathon.assignment.domain.consumer.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseProductRequest {

	@Valid
	List<PurchaseProductEntry> purchaseProducts;
}
