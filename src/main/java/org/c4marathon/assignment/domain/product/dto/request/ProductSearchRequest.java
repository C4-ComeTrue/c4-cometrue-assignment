package org.c4marathon.assignment.domain.product.dto.request;

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
		this.createdAt = setDefaultValue(createdAt, LocalDateTime.now());
		this.productId = setDefaultValue(productId, 0L);
		this.amount = setDefaultValue(amount, getDefaultAmount(sortType));
		this.orderCount = setDefaultValue(orderCount, Long.MAX_VALUE);
		this.score = setDefaultValue(score, 5.0);
		this.pageSize = pageSize;
	}

	private <T> T setDefaultValue(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	private Long getDefaultAmount(SortType sortType) {
		return sortType == SortType.PRICE_ASC ? 0L : Long.MAX_VALUE;
	}
}
