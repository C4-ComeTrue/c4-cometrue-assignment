package org.c4marathon.assignment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
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

	@Mock
	ChargeService chargeService;

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
	void 계좌_송금에_성공한다() {
		// given
		var accountId = 1L;
		var transferAccountNumber = "11-22";
		var amount = 10000L;
		var transferAmount = 1000L;
		var account = mock(Account.class);
		var transferAccount = mock(Account.class);

		given(account.getAmount()).willReturn(amount);
		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(accountRepository.findByAccountNumberWithWriteLock(anyString())).willReturn(Optional.of(transferAccount));

		// when
		accountService.transfer(accountId, transferAccountNumber, transferAmount);

		// then
		verify(account, times(1)).withdraw(transferAmount);
		verify(transferAccount, times(1)).charge(transferAmount);
	}


	@Test
	void 잔액이_부족하다면_자동_충전이_발생한다() {
		var accountId = 1L;
		var transferAccountNumber = "11-22";
		var amount = 100L;
		var transferAmount = 1000L;
		var account = mock(Account.class);
		var transferAccount = mock(Account.class);

		given(account.getAmount()).willReturn(amount);
		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(accountRepository.findByAccountNumberWithWriteLock(anyString())).willReturn(Optional.of(transferAccount));

		// when
		accountService.transfer(accountId, transferAccountNumber, transferAmount);

		// then
		verify(chargeService, times(1)).autoChargeByUnit(anyLong(), anyLong());
	}

	@Test
	void 계좌가_없다면_송금에_실패한다() {
		// given
		var accountId = 1L;
		var transferAccountNumber = "11-22";
		var transferAmount = 10000L;

		// when + then
		assertThatThrownBy(() -> accountService.transfer(accountId, transferAccountNumber, transferAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());
	}

	@Test
	void 송금_대상의_계좌가_없다면_송금에_실패한다() {
		// given
		var accountId = 1L;
		var transferAccountNumber = "11-22";
		var amount = 100000L;
		var transferAmount = 10000L;
		var account = mock(Account.class);

		// when
		given(account.getAmount()).willReturn(amount);
		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));

		assertThatThrownBy(() -> accountService.transfer(accountId, transferAccountNumber, transferAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());
	}
}
