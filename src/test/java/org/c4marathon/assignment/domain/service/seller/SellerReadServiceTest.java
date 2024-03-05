package org.c4marathon.assignment.domain.service.seller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.domain.seller.service.SellerReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

public class SellerReadServiceTest extends ServiceTestSupport {

	@InjectMocks
	private SellerReadService sellerReadService;

	@DisplayName("이메일로 배송 회사 조회 시")
	@Nested
	class ExistsByEmail {

		@DisplayName("email에 해당하는 회원이 존재하면 true를 반환한다.")
		@Test
		void returnTrue_when_existsSeller() {
			given(sellerRepository.existsByEmail(anyString())).willReturn(true);
			assertThat(sellerReadService.existsByEmail("email")).isTrue();
		}

		@DisplayName("email에 해당하는 회원이 존재하지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_notExistsSeller() {
			given(sellerRepository.existsByEmail(anyString())).willReturn(false);
			assertThat(sellerReadService.existsByEmail("email")).isFalse();
		}
	}
}
