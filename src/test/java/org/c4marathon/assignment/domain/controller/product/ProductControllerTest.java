package org.c4marathon.assignment.domain.controller.product;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.controller.ControllerTestSupport;
import org.c4marathon.assignment.domain.product.dto.request.ProductSearchRequest;
import org.c4marathon.assignment.domain.product.dto.response.ProductSearchEntry;
import org.c4marathon.assignment.domain.product.dto.response.ProductSearchResponse;
import org.c4marathon.assignment.global.constant.SortType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ProductControllerTest extends ControllerTestSupport {

	@DisplayName("상품 검색 시")
	@Nested
	class SearchProduct {

		private static final String REQUEST_URL = "/products";

		@BeforeEach
		void setUp() {
			given(productReadService.searchProduct(any(ProductSearchRequest.class)))
				.willReturn(new ProductSearchResponse(List.of(new ProductSearchEntry(1, "name", "des", 1, 1,
					LocalDateTime.now(), 0L, 0.0))));
		}

		@DisplayName("올바른 파라미터를 요청하면 성공한다.")
		@Test
		void success_when_validRequest() throws Exception {
			mockMvc.perform(get(REQUEST_URL)
					.param("keyword", "ab")
					.param("sortType", SortType.Newest.name())
					.param("pageSize", "1"))
				.andExpectAll(
					status().isOk(),
					jsonPath("$.productSearchEntries[0].id").value(1),
					jsonPath("$.productSearchEntries[0].name").value("name"),
					jsonPath("$.productSearchEntries[0].description").value("des"),
					jsonPath("$.productSearchEntries[0].amount").value(1),
					jsonPath("$.productSearchEntries[0].stock").value(1)
				);
		}

		@DisplayName("keyword가 null이거나 길이가 2보다 작으면 실패한다.")
		@ParameterizedTest
		@ValueSource(strings = {"a", "b"})
		@NullAndEmptySource
		void fail_when_keywordIsNullOrLessThanTwo(String keyword) throws Exception {
			mockMvc.perform(get(REQUEST_URL)
					.param("keyword", keyword)
					.param("sortType", SortType.Newest.name())
					.param("pageSize", "1"))
				.andExpectAll(
					status().isBadRequest()
				);
		}

		@DisplayName("sortType이 null이거나 존재하지 않은 값이면 실패한다.")
		@ParameterizedTest
		@ValueSource(strings = {"a", "b"})
		@NullAndEmptySource
		void fail_when_sortTypeIsNullOrInvalid(String sortType) throws Exception {
			mockMvc.perform(get(REQUEST_URL)
					.param("keyword", "ab")
					.param("sortType", sortType)
					.param("pageSize", "1"))
				.andExpectAll(
					status().isBadRequest()
				);
		}
	}
}
