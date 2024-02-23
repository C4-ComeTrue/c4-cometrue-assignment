package org.c4marathon.assignment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.common.utils.ChargeLimitUtils;
import org.c4marathon.assignment.domain.entity.Account;
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

	@InjectMocks
	ChargeService chargeService;

	@Test
	void 계좌_충전에_성공한다() {
		// given
		var accountId = 1L;
		var chargeAmount = 1000L;
		var accumulatedChargeAmount = 100000L;
		var totalAmount = 1000L;
		var account = mock(Account.class);

		given(accountRepository.findByIdWithWriteLock(anyLong())).willReturn(Optional.of(account));
		given(account.getChargeLimit()).willReturn(ChargeLimitUtils.BASIC_LIMIT);
		given(account.getAccumulatedChargeAmount()).willReturn(accumulatedChargeAmount);
		given(account.getAmount()).willReturn(totalAmount);

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
		assertThatThrownBy(() -> chargeService.charge(accountId,chargeAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());
	}


	@Test
	void 충전_한도를_넘는_경우_실패한다() {
		// given
		var accountId = 1L;
		var account = mock(Account.class);
		var chargeAmount = 10000L;

		given(accountRepository.findByIdWithWriteLock(anyLong())).willReturn(Optional.of(account));
		given(account.getChargeLimit()).willReturn(ChargeLimitUtils.BASIC_LIMIT);
		given(account.getAccumulatedChargeAmount()).willReturn(3000001L);

		// when + then
		assertThatThrownBy(() -> chargeService.charge(accountId, chargeAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.EXCEED_CHARGE_LIMIT.getMessage());
	}
}
