package org.c4marathon.assignment.bankaccount.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;
import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.exception.AccountException;
import org.c4marathon.assignment.bankaccount.message.util.RedisOperator;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.common.utils.ConstValue;
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
class MainAccountServiceTest {

	@InjectMocks
	MainAccountService mainAccountService;
	@Mock
	MainAccountRepository mainAccountRepository;

	@Mock
	SavingAccountRepository savingAccountRepository;
	@Mock
	DepositHandlerService depositHandlerService;
	@Mock
	RedisOperator redisOperator;

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
			MainAccount mainAccount = new MainAccount();
			mainAccount.setUpdatedAt(LocalDateTime.now());
			given(mainAccountRepository.findByPkForUpdate(mainAccountPk)).willReturn(Optional.of(mainAccount));
			// When
			long returnValue = mainAccountService.chargeMoney(mainAccountPk, money);

			// Then
			assertEquals(returnValue, accountMoney + money);
		}

		@Test
		@DisplayName("충전 한도를 초과하면 AccountException(CHARGE_LIMIT_EXCESS) 예외가 발생한다.")
		void request_with_over_charge_limit() {
			// Given
			MainAccount mainAccount = new MainAccount();
			mainAccount.setUpdatedAt(LocalDateTime.now());
			mainAccount.charge(ConstValue.LimitConst.CHARGE_LIMIT);
			given(mainAccountRepository.findByPkForUpdate(mainAccountPk)).willReturn(Optional.of(mainAccount));

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
			given(mainAccountRepository.findByPkForUpdate(mainAccountPk)).willReturn(Optional.empty());

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

			SavingAccount savingAccount = new SavingAccount("free", 500);
			given(savingAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(savingAccount));
			MainAccount mainAccount = new MainAccount();
			mainAccount.setUpdatedAt(LocalDateTime.now());
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
			MainAccount mainAccount = new MainAccount();
			mainAccount.setUpdatedAt(LocalDateTime.now());
			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(mainAccount));
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
			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.empty());

			// When
			AccountException accountException = assertThrows(AccountException.class,
				() -> mainAccountService.sendToSavingAccount(1, 1, sendMoney));

			// Then
			assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND.name(), accountException.getErrorName());
		}
	}

	@Nested
	@DisplayName("메인 계좌 정보 조회 테스트")
	class GetMainAccountInfo {

		@Test
		@DisplayName("메인 계좌 정보 조회 성공 테스트")
		void request_with_valid_mainAccountPk() {
			// Given
			MainAccount mainAccount = new MainAccount();
			given(mainAccountRepository.findById(any())).willReturn(Optional.of(mainAccount));

			// When
			MainAccountResponseDto mainAccountInfo = mainAccountService.getMainAccountInfo(anyLong());
			Optional<MainAccount> byId = mainAccountRepository.findById(0L);

			// Then
			assertEquals(mainAccountInfo.money(), mainAccount.getMoney());
		}

		@Test
		@DisplayName("메인 계좌 정보 조회 실패 테스트")
		void request_with_non_valid_mainAccountPk() {
			// Given
			given(mainAccountRepository.findById(anyLong())).willReturn(Optional.empty());

			// When
			AccountException accountException = assertThrows(AccountException.class,
				() -> mainAccountService.getMainAccountInfo(1L));

			// Then
			assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND.name(), accountException.getErrorName());
		}
	}

	@Nested
	@DisplayName("메인 계좌 간 이체 테스트")
	class SendToMainAccount {

		@Test
		@DisplayName("이체 성공 테스트")
		void send_to_other_account_success() {
			// Given
			long senderPk = 1L;
			long depositPk = 2L;
			long money = 1000;
			MainAccount mainAccount = new MainAccount();
			mainAccount.setUpdatedAt(LocalDateTime.now());
			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(mainAccount));

			// When
			mainAccountService.sendToOtherAccount(senderPk, depositPk, money);

			// Then
			then(mainAccountRepository).should(times(1)).findByPkForUpdate(anyLong());
			then(mainAccountRepository).should(times(1)).save(any());
			then(redisOperator).should(times(1)).addStream(any(), anyLong(), anyLong(), anyLong());
		}

		@Test
		@DisplayName("요청한 계좌의 정보가 없으면 AccountException(ACCOUNT_NOT_FOUND) 예외가 발생한다.")
		void request_with_non_valid_accountPk() {
			// Given
			long senderPk = 1L;
			long depositPk = 2L;
			long money = 1000;
			
			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.empty());

			// When
			AccountException accountException = assertThrows(AccountException.class,
				() -> mainAccountService.sendToOtherAccount(senderPk, depositPk, money));

			// Then
			assertEquals(accountException.getErrorName(), AccountErrorCode.ACCOUNT_NOT_FOUND.name());
		}

		@Test
		@DisplayName("충전 한도를 초과하면 AccountException(CHARGE_LIMIT_EXCESS) 예외가 발생한다.")
		void request_with_no_spare_money() {
			// Given
			long senderPk = 1L;
			long depositPk = 2L;
			long money = 1000;
			MainAccount mainAccount = new MainAccount();
			mainAccount.setUpdatedAt(LocalDateTime.now());
			mainAccount.charge(ConstValue.LimitConst.CHARGE_LIMIT);
			mainAccount.minusMoney(ConstValue.LimitConst.CHARGE_LIMIT);

			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(mainAccount));

			// When
			AccountException accountException = assertThrows(AccountException.class,
				() -> mainAccountService.sendToOtherAccount(senderPk, depositPk, money));

			// Then
			assertEquals(accountException.getErrorName(), AccountErrorCode.CHARGE_LIMIT_EXCESS.name());
		}

		@Test
		@DisplayName("잔고를 초과한 금액은 10000원 단위로 충전후 이체를 진행한다.")
		void request_with_over_money() {
			// Given
			long senderPk = 1L;
			long depositPk = 2L;
			long money = 1000;
			MainAccount mainAccount = new MainAccount();
			mainAccount.setUpdatedAt(LocalDateTime.now());
			given(mainAccountRepository.findByPkForUpdate(anyLong())).willReturn(Optional.of(mainAccount));

			// When
			mainAccountService.sendToOtherAccount(senderPk, depositPk, money);

			// Then
			assertEquals(ConstValue.LimitConst.CHARGE_AMOUNT - money, mainAccount.getMoney());
		}
	}
}
