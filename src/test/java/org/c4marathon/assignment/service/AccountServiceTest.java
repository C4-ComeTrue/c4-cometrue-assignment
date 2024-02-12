package org.c4marathon.assignment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigInteger;
import java.util.Optional;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.ChargeLimit;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	AccountRepository accountRepository;

	@Mock
	MemberRepository memberRepository;

	@InjectMocks
	AccountService accountService;

	@Test
	void 계좌_생성에_성공한다() {
		// given
		var accountId = 1L;
		var member = mock(Member.class);
		var account = mock(Account.class);

		given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
		given(accountRepository.save(any())).willReturn(account);
		given(account.getId()).willReturn(accountId);

		// when
		var result = accountService.createAccount(1L, "name", "accountNumber");

		// then
		assertThat(result.id()).isEqualTo(accountId);
	}

	@Test
	void 계좌_충전에_성공한다() {
		// given
		var accountId = 1L;
		var chargeAmount = 1000;
		var accumulatedChargeAmount = 100000;
		var totalAmount = 1000;
		var account = mock(Account.class);

		given(accountRepository.findByIdWithWriteLock(anyLong())).willReturn(Optional.of(account));
		given(account.getChargeLimit()).willReturn(ChargeLimit.DAY_BASIC_LIMIT);
		given(account.getAccumulatedChargeAmount()).willReturn(accumulatedChargeAmount);
		given(account.getAmount()).willReturn(BigInteger.valueOf(totalAmount));

		// when
		var result = accountService.charge(accountId, chargeAmount);

		// then
		assertThat(result.totalAmount()).isEqualTo(totalAmount);
	}

	@Test
	void 계좌가_존재하지_않는_경우_실패한다() {
		// given
		var accountId = 1L;
		var chargeAmount = 1000;

		// when + then
		assertThatThrownBy(() -> accountService.charge(accountId,chargeAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());
	}


	@Test
	void 충전_한도를_넘는_경우_실패한다() {
		// given
		var accountId = 1L;
		var account = mock(Account.class);
		var chargeAmount = 10000;

		given(accountRepository.findByIdWithWriteLock(anyLong())).willReturn(Optional.of(account));
		given(account.getChargeLimit()).willReturn(ChargeLimit.DAY_BASIC_LIMIT);
		given(account.getAccumulatedChargeAmount()).willReturn(3000001);

		// when + then
		assertThatThrownBy(() -> accountService.charge(accountId, chargeAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.EXCEED_CHARGE_LIMIT.getMessage());
	}
}
