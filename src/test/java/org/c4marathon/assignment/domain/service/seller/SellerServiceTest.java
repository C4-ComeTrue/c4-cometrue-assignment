package org.c4marathon.assignment.domain.service.seller;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.dto.request.PutProductRequest;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.service.SellerService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class SellerServiceTest extends ServiceTestSupport {

	@Autowired
	private SellerService sellerService;
	@MockBean
	private ProductReadService productReadService;

	@DisplayName("회원 가입 시")
	@Nested
	class Signup {

		@DisplayName("가입된 email이 존재하지 않으면 성공한다.")
		@Test
		void success_when_emailNotExists() {
			SignUpRequest request = createRequest();

			sellerService.signup(request);
			List<Seller> sellers = sellerRepository.findAll();

			assertThat(sellers).hasSize(1);
			assertThat(sellers.get(0).getEmail()).isEqualTo(request.email());
		}

		@DisplayName("가입된 email이 존재하면 예외를 반환한다.")
		@Test
		void fail_when_emailExists() {
			SignUpRequest request = createRequest();
			sellerRepository.save(Seller.builder()
				.email(request.email())
				.build());

			ErrorCode errorCode = ALREADY_SELLER_EXISTS;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> sellerService.signup(request))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		private SignUpRequest createRequest() {
			return new SignUpRequest("email", "address");
		}
	}

	@DisplayName("상품 업로드 시")
	@Nested
	class PutProduct {

		private Seller seller;

		@BeforeEach
		void setUp() {
			seller = sellerRepository.save(Seller.builder()
				.email("email")
				.build());
		}

		@DisplayName("이미 존재하는 상품 이름이면 예외를 반환한다.")
		@Test
		void throwException_when_alreadyExists() {
			given(productReadService.existsByNameAndSeller(anyString(), any(Seller.class)))
				.willReturn(true);

			ErrorCode errorCode = ALREADY_PRODUCT_NAME_EXISTS;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> sellerService.putProduct(createRequest(), seller))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("유일한 상품 이름과 판매자 조합이면, 업로드에 성공한다.")
		@Test
		void successPutProduct_when_uniqueNameAndSeller() {
			PutProductRequest request = createRequest();
			given(productReadService.existsByNameAndSeller(anyString(), any(Seller.class)))
				.willReturn(false);

			sellerService.putProduct(request, seller);
			List<Product> products = productRepository.findAll();

			assertThat(products).hasSize(1);
			assertThat(products.get(0).getName()).isEqualTo(request.name());
		}

		private PutProductRequest createRequest() {
			return new PutProductRequest("name", "description", 100L, 100);
		}
	}
}
