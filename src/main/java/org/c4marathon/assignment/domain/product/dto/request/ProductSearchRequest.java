package org.c4marathon.assignment.domain.product.dto.request;

import static java.util.Objects.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.global.constant.SortType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record ProductSearchRequest(
	@NotNull @Size(min = 2, message = "keyword length less than 2") String keyword,
	@NotNull SortType sortType,
	LocalDateTime createdAt,
	Long productId,
	Long amount,
	Long orderCount,
	Double score,
	int pageSize
) {

	@Builder
	public ProductSearchRequest(
		String keyword,
		SortType sortType,
		LocalDateTime createdAt,
		Long productId,
		Long amount,
		Long orderCount,
		Double score,
		int pageSize
	) {
		this.keyword = keyword;
		this.sortType = sortType;
		this.createdAt = requireNonNullElse(createdAt, LocalDateTime.now());
		this.productId = requireNonNullElse(productId, Long.MIN_VALUE);
		this.amount = requireNonNullElse(amount, getDefaultAmount(sortType));
		this.orderCount = requireNonNullElse(orderCount, Long.MAX_VALUE);
		this.score = requireNonNullElse(score, Double.MAX_VALUE);
		this.pageSize = pageSize;
	}

	private Long getDefaultAmount(SortType sortType) {
		return sortType == SortType.PRICE_ASC ? Long.MIN_VALUE : Long.MAX_VALUE;
	}
}
