package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.exception.AccountException;
import org.c4marathon.assignment.bankaccount.product.ProductManager;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SavingAccountServiceImplTest {

	@InjectMocks
	SavingAccountServiceImpl savingAccountService;

	@Mock
	MemberRepository memberRepository;
	@Mock
	SavingAccountRepository savingAccountRepository;
	@Mock
	ProductManager productManager;

	@Nested
	@DisplayName("적금 계좌 생성 테스트")
	class CreateSavingAccount {

		private long memberPk = 1L;

		@Test
		@DisplayName("존재하는 적금 상품 계좌를 생성할 경우 계좌가 정상으로 생성된다.")
		void request_with_exist_product() {
			// Given
			String productName = "free";
			Member member = new Member();
			given(memberRepository.getReferenceById(anyLong())).willReturn(member);
			given(productManager.getRate(productName)).willReturn(anyInt());

			// When
			savingAccountService.create(memberPk, productName);

			// Then
			then(memberRepository).should(times(1)).getReferenceById(memberPk);
			then(savingAccountRepository).should(times(1)).save(any());
			then(productManager).should(times(1)).getRate(productName);
		}

		@Test
		@DisplayName("존재하지 않는 적금 상품 계좌를 생성할 경우 AccountException(PRODUCT_NOT_FOUND) 예외가 발생한다.")
		void request_with_not_exist_product() {
			// Given
			String productName = "non exist product";
			given(productManager.getRate(productName)).willReturn(null);

			// When
			AccountException accountException = assertThrows(AccountException.class, () -> {
				savingAccountService.create(memberPk, productName);
			});

			// Then
			assertEquals(AccountErrorCode.PRODUCT_NOT_FOUND.name(), accountException.getErrorName());
		}
	}
}
