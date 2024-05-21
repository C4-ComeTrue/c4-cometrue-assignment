package org.c4marathon.assignment.domain.product.dto.request;

import java.time.LocalDateTime;

import org.c4marathon.assignment.global.constant.SortType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record ProductSearchRequest(
	@NotNull @Size(min = 2, message = "keyword length less than 2") String keyword,
	@NotNull SortType sortType,
	LocalDateTime lastCreatedAt,
	Long lastProductId,
	Long lastAmount,
	Long lastOrderCount,
	Double lastScore,
	int pageSize
) {

	@Builder
	public ProductSearchRequest(
		String keyword,
		SortType sortType,
		LocalDateTime lastCreatedAt,
		Long lastProductId,
		Long lastAmount,
		Long lastOrderCount,
		Double lastScore,
		int pageSize
	) {
		this.keyword = keyword;
		this.sortType = sortType;
		this.lastCreatedAt = setDefaultValue(lastCreatedAt, LocalDateTime.now());
		this.lastProductId = setDefaultValue(lastProductId, 0L);
		this.lastAmount = setDefaultValue(lastAmount, getDefaultLastAmount(sortType));
		this.lastOrderCount = setDefaultValue(lastOrderCount, Long.MAX_VALUE);
		this.lastScore = setDefaultValue(lastScore, 5.0);
		this.pageSize = pageSize;
	}

	private <T> T setDefaultValue(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	private Long getDefaultLastAmount(SortType sortType) {
		return sortType == SortType.PriceAsc ? 0L : Long.MAX_VALUE;
	}
}
