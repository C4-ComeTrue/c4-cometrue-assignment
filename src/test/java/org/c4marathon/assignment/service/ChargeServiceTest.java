package org.c4marathon.assignment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyBoolean;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.common.utils.ChargeLimitUtils;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.ChargeLinkedAccount;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.ChargeLinkedAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChargeServiceTest {

	@Mock
	AccountRepository accountRepository;

	@Mock
	ChargeLinkedAccountRepository linkedAccountRepository;

	@Mock
	Clock clock;

	@InjectMocks
	ChargeService chargeService;

	LocalDate currentDate = LocalDate.of(2024, 2, 25);  // 현재 시간 고정
	LocalDate chargedDate = LocalDate.of(2024, 2, 24);

	@Test
	void 충전_연동_계좌를_등록한다() {
		// given
		var accountId = 1L;
		var account = mock(Account.class);
		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));

		// when
		chargeService.registerChargeAccount(accountId, "우리", "1111-2222", true);

		// then
		verify(linkedAccountRepository, times(1)).save(any());
	}

	@Test
	void 만원_단위로_충전에_성공한다() {
		// given
		var accountId = 1L;
		var chargeAmount = 12000L;   // 20000원 자동 충전
		var accumulatedChargeAmount = 100000L;
		var linkedAccountAmount = 20000L;

		var linkedAccount = mock(ChargeLinkedAccount.class);
		var account = mock(Account.class);

		mockCurrentLocalDate();

		given(linkedAccountRepository.findByAccountIdAndMain(anyLong(), anyBoolean())).willReturn(Optional.of(linkedAccount));

		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(account.getChargeLimit()).willReturn(ChargeLimitUtils.BASIC_LIMIT);
		given(account.getAccumulatedChargeAmount()).willReturn(accumulatedChargeAmount);
		given(account.getChargeUpdatedAt()).willReturn(chargedDate);

		// when
		chargeService.autoChargeByUnit(accountId, chargeAmount);

		// then
		verify(linkedAccount, times(1)).withdraw(anyLong());
	}

	@Test
	void 충전_연동_계좌에_돈이_부족하면_자동_충전이_실패한다() {
		// given
		var accountId = 1L;
		var chargeAmount = 12000L;   // 20000원 충전 필요
		var linkedAccountAmount = 19000L;
		var linkedAccount = mock(ChargeLinkedAccount.class);

		given(linkedAccountRepository.findByAccountIdAndMain(anyLong(), anyBoolean())).willReturn(Optional.of(linkedAccount));
		given(linkedAccount.isAmountLackToWithDraw(anyLong())).willReturn(true);

		// when + then
		assertThatThrownBy(() -> chargeService.autoChargeByUnit(accountId, chargeAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.ACCOUNT_LACK_OF_AMOUNT.name());
	}

	@Test
	void 충전_연동_계좌가_없다면_자동_충전이_실패한다() {
		// given
		var accountId = 1L;
		var chargeAmount = 1000L;

		// when + then
		assertThatThrownBy(() -> chargeService.autoChargeByUnit(accountId, chargeAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_CHARGE_LINKED_ACCOUNT.name());
	}

	@Test
	void 계좌_충전에_성공한다() {
		// given
		var accountId = 1L;
		var chargeAmount = 1000L;
		var accumulatedChargeAmount = 100000L;
		var totalAmount = 1000L;
		var account = mock(Account.class);

		mockCurrentLocalDate();

		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(accountRepository.findAmount(anyLong())).willReturn(totalAmount);
		given(account.getChargeLimit()).willReturn(ChargeLimitUtils.BASIC_LIMIT);
		given(account.getAccumulatedChargeAmount()).willReturn(accumulatedChargeAmount);
		given(account.getChargeUpdatedAt()).willReturn(chargedDate);

		// when
		var result = chargeService.charge(accountId, chargeAmount);

		// then
		assertThat(result.totalAmount()).isEqualTo(totalAmount);
	}

	@Test
	void 계좌가_존재하지_않는_경우_실패한다() {
		// given
		var accountId = 1L;
		var chargeAmount = 1000L;

		// when + then
		assertThatThrownBy(() -> chargeService.charge(accountId, chargeAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());
	}

	@Test
	void 충전_한도를_넘는_경우_실패한다() {
		// given
		var accountId = 1L;
		var account = mock(Account.class);
		var chargeAmount = 10000L;

		mockCurrentLocalDate();

		given(account.getChargeUpdatedAt()).willReturn(chargedDate);
		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(account.getChargeLimit()).willReturn(ChargeLimitUtils.BASIC_LIMIT);
		given(account.getAccumulatedChargeAmount()).willReturn(3000001L);

		// when + then
		assertThatThrownBy(() -> chargeService.charge(accountId, chargeAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.EXCEED_CHARGE_LIMIT.getMessage());
	}

	@Test
	void 날짜가_바뀌면_충전_한도가_초기화_된다() {
		// given
		var accountId = 1L;
		var account = mock(Account.class);
		var chargeAmount = 10000L;
		var accumulatedChargeAmount = 10000L;

		mockCurrentLocalDate();

		given(account.getChargeUpdatedAt()).willReturn(chargedDate);
		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(account.getChargeLimit()).willReturn(ChargeLimitUtils.BASIC_LIMIT);
		given(account.getAccumulatedChargeAmount()).willReturn(accumulatedChargeAmount);

		// when
		chargeService.charge(accountId, chargeAmount);

		// then
		verify(account, times(1)).initializeChargeAmount();
	}

	private void mockCurrentLocalDate() {
		var fixedClock = Clock.fixed(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
		given(clock.instant()).willReturn(fixedClock.instant());
		given(clock.getZone()).willReturn(fixedClock.getZone());
	}
}
