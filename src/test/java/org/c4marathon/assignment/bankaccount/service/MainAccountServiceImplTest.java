package org.c4marathon.assignment.bankaccount.service;

import java.util.Optional;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.exception.AccountException;
import org.c4marathon.assignment.bankaccount.limit.ChargeLimitManager;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MainAccountServiceImplTest {

	@InjectMocks
	MainAccountServiceImpl mainAccountService;
	@Mock
	MainAccountRepository mainAccountRepository;
	@Mock
	ChargeLimitManager chargeLimitManager;

	@Nested
	@DisplayName("메인 계좌 충전 테스트")
	class Charge {
		long mainAccountPk = 1L;
		int money = 1000;
		int accountMoney = 0;

		@Test
		@DisplayName("계좌가 있고 충전 한도를 벗어나지 않으면 정상 충전된다.")
		void request_with_valid_account_and_charging_limit() {
			// Given
			given(chargeLimitManager.charge(mainAccountPk, money)).willReturn(true);
			MainAccount mainAccount = MainAccount.builder()
				.money(accountMoney)
				.build();
			given(mainAccountRepository.findByIdForUpdate(mainAccountPk)).willReturn(Optional.of(mainAccount));
			// When
			int returnValue = mainAccountService.chargeMoney(mainAccountPk, money);

			// Then
			assertEquals(returnValue, accountMoney + money);
		}

		@Test
		@DisplayName("충전 한도를 초과하면 AccountException(CHARGE_LIMIT_EXCESS) 예외가 발생한다.")
		void request_with_over_charge_limit() {
			// Given
			given(chargeLimitManager.charge(mainAccountPk, money)).willReturn(false);

			// When
			AccountException accountException = assertThrows(AccountException.class, () -> {
				mainAccountService.chargeMoney(mainAccountPk, money);
			});

			// Then
			assertEquals(AccountErrorCode.CHARGE_LIMIT_EXCESS.name(), accountException.getErrorName());
		}

		@Test
		@DisplayName("메인 계좌가 생성되지 않은 사용자라면 AccountException(ACCOUNT_NOT_FOUND) 예외가 발생한다.")
		void request_with_no_main_account() {
			// Given
			given(chargeLimitManager.charge(mainAccountPk, money)).willReturn(true);
			given(mainAccountRepository.findByIdForUpdate(mainAccountPk)).willReturn(Optional.empty());

			// When
			AccountException accountException = assertThrows(AccountException.class, () -> {
				mainAccountService.chargeMoney(mainAccountPk, money);
			});

			// Then
			assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND.name(), accountException.getErrorName());
		}

	}
}
