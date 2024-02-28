package org.c4marathon.assignment.domain.service.product;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

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
}
