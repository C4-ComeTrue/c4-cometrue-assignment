package org.c4marathon.assignment.domain.service.product;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductReadServiceTest extends ServiceTestSupport {

	@Autowired
	private ProductReadService productReadService;
	private Product product;
	private Seller seller;

	@BeforeEach
	void setUp() {
		seller = sellerRepository.save(Seller.builder()
			.email("email")
			.build());
		product = productRepository.save(Product.builder()
			.amount(100L)
			.stock(100)
			.name("name")
			.seller(seller)
			.description("description")
			.build());
	}

	@DisplayName("상품 이름, 판매자로 조회 시")
	@Nested
	class ExistsByNameAndSeller {

		@DisplayName("name, seller에 해당하는 Product가 존재하면 true를 반환한다.")
		@Test
		void returnTrue_when_exists() {
			assertThat(productReadService.existsByNameAndSeller(product.getName(), seller)).isTrue();
		}

		@DisplayName("name, seller에 해당하는 Product가 존재하지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_notExists() {
			assertThat(productReadService.existsByNameAndSeller(product.getName() + "A", seller)).isFalse();
		}
	}

	@DisplayName("id로 조회 시")
	@Nested
	class FindById {

		@DisplayName("id에 해당하는 Product가 존재하면 반환한다.")
		@Test
		void returnProduct_when_exists() {
			Product find = productReadService.findById(product.getId());

			assertThat(find.getId()).isEqualTo(product.getId());
		}

		@DisplayName("id에 해당하는 Product가 존재하지 않으면 예외를 반환한다.")
		@Test
		void throwException_when_notExists() {

			BaseException exception = new BaseException(PRODUCT_NOT_FOUND);
			assertThatThrownBy(() -> productReadService.findById(product.getId() + 1))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
