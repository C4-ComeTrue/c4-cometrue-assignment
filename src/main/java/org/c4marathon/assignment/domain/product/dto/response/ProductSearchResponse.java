package org.c4marathon.assignment.domain.product.dto.response;

import java.util.List;

import org.c4marathon.assignment.domain.product.entity.Product;

public record ProductSearchResponse(
	List<ProductSearchEntry> productSearchEntries
) {
	public static ProductSearchResponse of(List<Product> products) {
		return new ProductSearchResponse(products.stream()
			.map(product -> new ProductSearchEntry(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getAmount(),
				product.getStock(),
				product.getCreatedAt(),
				product.getOrderCount(),
				product.getAvgScore()))
			.toList());
	}
}
