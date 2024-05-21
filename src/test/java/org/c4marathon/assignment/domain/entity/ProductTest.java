package org.c4marathon.assignment.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.constant.ProductStatus.*;

import org.c4marathon.assignment.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProductTest {

	@DisplayName("Product Entity test")
	@Nested
	class ProductEntityTest {

		private Product product;

		@BeforeEach
		void setUp() {
			product = Product.builder()
				.name("name")
				.description("des")
				.amount(100L)
				.stock(100)
				.seller(null)
				.build();
		}

		@DisplayName("consumer entity의 메서드 수행 시, 필드가 변경된다.")
		@Test
		void updateField_when_invokeEntityMethod() {
			assertThat(product.getName()).isEqualTo("name");
			assertThat(product.getDescription()).isEqualTo("des");
			assertThat(product.getAmount()).isEqualTo(100L);
			assertThat(product.getStock()).isEqualTo(100);
			assertThat(product.getProductStatus()).isEqualTo(IN_STOCK);
			assertThat(product.getOrderCount()).isZero();
			assertThat(product.getAvgScore()).isZero();

			product.decreaseStock(100);
			assertThat(product.getProductStatus()).isEqualTo(OUT_OF_STOCK);
			assertThat(product.isSoldOut()).isTrue();
			product.increaseOrderCount();
			assertThat(product.getOrderCount()).isEqualTo(1);
			product.updateAvgScore(1.0);
			assertThat(product.getAvgScore()).isEqualTo(1.0);
		}
	}
}
