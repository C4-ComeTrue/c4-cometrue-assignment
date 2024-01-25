package org.c4marathon.assignment.bankaccount.service;

import java.util.Optional;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.exception.AccountException;
import org.c4marathon.assignment.bankaccount.limit.ChargeLimitManager;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
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
class MainAccountServiceImplTest {

	@InjectMocks
	MainAccountServiceImpl mainAccountService;
	@Mock
	MainAccountRepository mainAccountRepository;
	@Mock
	ChargeLimitManager chargeLimitManager;
	@Mock
	SavingAccountRepository savingAccountRepository;

	@Nested
	@DisplayName("메인 계좌 충전 테스트")
	class ChargeMoney {
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

	@Nested
	@DisplayName("메인 계좌에서 적금 계좌로 이체 테스트")
	class SendToSavingAccount {
		private int sendMoney = 1000;
		private int myMoney = 10000;

		@Test
		@DisplayName("메인 계좌와 적금 계좌가 모두 있고, 잔액이 이체 금액 이상이면 적금 계좌로 이체가 성공한다.")
		void request_with_exist_accounts_and_valid_send_money() {
			// Given

			SavingAccount savingAccount = new SavingAccount();
			savingAccount.init("free", 500);
			given(savingAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(savingAccount));
			MainAccount mainAccount = MainAccount.builder()
				.money(myMoney)
				.build();
			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(mainAccount));

			// When
			mainAccountService.sendToSavingAccount(1, 1, sendMoney);

			// Then
			assertEquals(savingAccount.getSavingMoney(), sendMoney);
			assertEquals(mainAccount.getMoney(), myMoney - sendMoney);
		}

		@Test
		@DisplayName("적금 계좌가 없으면 AccountException(ACCOUNT_NOT_FOUND) 예외가 발생한다.")
		void request_with_no_main_account() {
			// Given
			given(savingAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.empty());

			// When
			AccountException accountException = assertThrows(AccountException.class,
				() -> mainAccountService.sendToSavingAccount(1, 1, sendMoney));

			// Then
			assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND.name(), accountException.getErrorName());
		}

		@Test
		@DisplayName("메인 계좌가 없으면 AccountException(ACCOUNT_NOT_FOUND) 예외가 발생한다.")
		void request_with_no_saving_account() {
			// Given
			given(savingAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(new SavingAccount()));
			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.empty());

			// When
			AccountException accountException = assertThrows(AccountException.class,
				() -> mainAccountService.sendToSavingAccount(1, 1, sendMoney));

			// Then
			assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND.name(), accountException.getErrorName());
		}

		@Test
		@DisplayName("메인 계좌 잔고가 이체 금액보다 적으면 AccountException(INVALID_MONEY_SEND) 예외가 발생한다.")
		void request_with_invalid_send_money() {
			// Given
			SavingAccount savingAccount = new SavingAccount();
			savingAccount.init("free", 500);
			given(savingAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(savingAccount));
			MainAccount mainAccount = MainAccount.builder()
				.money(0)
				.build();
			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(mainAccount));

			// When
			AccountException accountException = assertThrows(AccountException.class,
				() -> mainAccountService.sendToSavingAccount(1, 1, sendMoney));

			// Then
			assertEquals(AccountErrorCode.INVALID_MONEY_SEND.name(), accountException.getErrorName());
		}
	}
}
