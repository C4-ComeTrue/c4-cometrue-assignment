package org.c4marathon.assignment.domain.service.product;

import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.product.dto.request.ProductSearchRequest;
import org.c4marathon.assignment.domain.product.dto.response.ProductSearchEntry;
import org.c4marathon.assignment.domain.product.dto.response.ProductSearchResponse;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.constant.SortType;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

public class ProductReadServiceTest extends ServiceTestSupport {

	@InjectMocks
	private ProductReadService productReadService;

	@DisplayName("상품 이름, 판매자로 조회 시")
	@Nested
	class ExistsByNameAndSeller {

		@DisplayName("name, seller에 해당하는 Product가 존재하면 true를 반환한다.")
		@Test
		void returnTrue_when_exists() {
			given(productRepository.existsByNameAndSeller(anyString(), any(Seller.class))).willReturn(true);
			assertThat(productReadService.existsByNameAndSeller("", seller)).isTrue();
		}

		@DisplayName("name, seller에 해당하는 Product가 존재하지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_notExists() {
			given(productRepository.existsByNameAndSeller(anyString(), any(Seller.class))).willReturn(false);
			assertThat(productReadService.existsByNameAndSeller("", seller)).isFalse();
		}
	}

	@DisplayName("id로 조회 시")
	@Nested
	class FindById {

		@DisplayName("id에 해당하는 Product가 존재하면 반환한다.")
		@Test
		void returnProduct_when_exists() {
			given(productRepository.findByIdJoinFetch(anyLong())).willReturn(Optional.of(product));
			productReadService.findById(1L);
			then(productRepository)
				.should(times(1))
				.findByIdJoinFetch(anyLong());
		}

		@DisplayName("id에 해당하는 Product가 존재하지 않으면 예외를 반환한다.")
		@Test
		void throwException_when_notExists() {
			given(productRepository.findByIdJoinFetch(anyLong())).willReturn(Optional.empty());
			ErrorCode errorCode = PRODUCT_NOT_FOUND;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> productReadService.findById(product.getId() + 1))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}

	@DisplayName("reviewCount 조회 시")
	@Nested
	class FindReviewCount {

		@DisplayName("product_id에 해당하는 reviewCount를 조회한다.")
		@Test
		void returnReviewCount_when_exists() {
			given(productRepository.findReviewCount(anyLong())).willReturn(10L);
			final long reviewCount = productReadService.findReviewCount(product.getId());
			assertThat(reviewCount).isEqualTo(10L);
		}
	}

	@DisplayName("상품 검색 시")
	@Nested
	@SpringBootTest
	class SearchProduct {

		@DisplayName("정렬된 순서로 조회된다.")
		@ParameterizedTest
		@EnumSource(value = SortType.class)
		void returnSortedList_when_searchProduct(SortType sortType) {
			ProductSearchRequest request = new ProductSearchRequest("ab", sortType, null, null, null, null, null, 100);
			ProductSearchResponse response = productReadService.searchProduct(request);
			List<ProductSearchEntry> result = response.productSearchEntries();

			switch (sortType) {
				case TOP_RATED ->
					assertThat(result).isSortedAccordingTo(comparing(ProductSearchEntry::avgScore).reversed());
				case NEWEST ->
					assertThat(result).isSortedAccordingTo(comparing(ProductSearchEntry::createdAt).reversed());
				case PRICE_ASC -> assertThat(result).isSortedAccordingTo(comparing(ProductSearchEntry::amount));
				case PRICE_DESC ->
					assertThat(result).isSortedAccordingTo(comparing(ProductSearchEntry::amount).reversed());
				case POPULARITY ->
					assertThat(result).isSortedAccordingTo(comparing(ProductSearchEntry::orderCount).reversed());
			}
		}
	}
}
