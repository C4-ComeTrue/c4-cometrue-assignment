package org.c4marathon.assignment.domain.consumer.dto.request;

import java.util.List;

import jakarta.validation.Valid;

public record PurchaseProductRequest(
	List<@Valid PurchaseProductEntry> purchaseProducts
) {
}
