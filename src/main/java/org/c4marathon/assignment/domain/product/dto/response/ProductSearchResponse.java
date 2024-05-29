package org.c4marathon.assignment.domain.product.dto.response;

import java.util.List;

public record ProductSearchResponse(
	List<ProductSearchEntry> productSearchEntries
) {
}
