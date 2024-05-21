package org.c4marathon.assignment.domain.service.product;

import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.repository.ProductRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
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

		@Autowired
		private ProductRepository productRepository;

		@DisplayName("")
		@ParameterizedTest
		@EnumSource(value = SortType.class)
		void test(SortType sortType) {
			List<Product> result = switch (sortType) {
				case TopRated -> productRepository.findByTopRated("%ab%", 0.0, 0L, 100);
				case Newest -> productRepository.findByNewest("%ab%", LocalDateTime.now(), 0L, 100);
				case PriceAsc -> productRepository.findByPriceAsc("%ab%", 0L, 0L, 100);
				case PriceDesc -> productRepository.findByPriceDesc("%ab%", 0L, 0L, 100);
				case Popularity -> productRepository.findByPopularity("%ab%", 0L, 0L, 100);
			};

			switch (sortType) {
				case TopRated -> assertThat(result).isSortedAccordingTo(comparing(Product::getAvgScore).reversed());
				case Newest -> assertThat(result).isSortedAccordingTo(comparing(Product::getCreatedAt).reversed());
				case PriceAsc -> assertThat(result).isSortedAccordingTo(comparing(Product::getAmount));
				case PriceDesc -> assertThat(result).isSortedAccordingTo(comparing(Product::getAmount).reversed());
				case Popularity -> assertThat(result).isSortedAccordingTo(comparing(Product::getOrderCount).reversed());
			}
		}
	}
}
