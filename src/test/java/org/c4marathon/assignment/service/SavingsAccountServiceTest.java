package org.c4marathon.assignment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.SavingsType;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.domain.entity.SavingsAccount;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.c4marathon.assignment.repository.SavingsAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsAccountServiceTest {

	@Mock
	AccountRepository accountRepository;

	@Mock
	MemberRepository memberRepository;

	@Mock
	SavingsAccountRepository savingsAccountRepository;

	@InjectMocks
	SavingsAccountService savingsAccountService;


	@Test
	void 적금_계좌_생성에_성공한다() {
		// given
		var accountId = 1L;
		var withdrawAmount = 10000L;
		var savingType = SavingsType.REGULAR;
		var member = mock(Member.class);
		var savingsAccount = mock(SavingsAccount.class);

		given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
		given(savingsAccountRepository.save(any())).willReturn(savingsAccount);
		given(savingsAccount.getId()).willReturn(accountId);

		// when
		var res = savingsAccountService.createSavingsAccount(1L, "name", withdrawAmount, savingType);

		// then
		assertThat(res.id()).isEqualTo(accountId);
	}

	@Test
	void 메인_에서_적금_계좌로_이체에_성공한다() {
		// given
		var withdrawAmount = 10000L;
		var mainAccountAmount = 50000L;
		var account = mock(Account.class);
		var savingsAccount = mock(SavingsAccount.class);

		given(accountRepository.findByMemberIdWithWriteLock(anyLong())).willReturn(Optional.of(account));
		given(savingsAccountRepository.findByMemberIdWithWriteLock(anyLong())).willReturn(Optional.of(savingsAccount));
		given(savingsAccount.getWithdrawAmount()).willReturn(withdrawAmount);
		given(account.getAmount()).willReturn(mainAccountAmount);

		// when
		savingsAccountService.transferForRegularSavings(1L);

		// then
		verify(account, times(1)).withdraw(withdrawAmount);
		verify(savingsAccount, times(1)).charge(withdrawAmount);
	}

	@Test
	void 메인_계좌가_존재하지_않으면_이체에_실패한다() {
		// given
		var memberId = 1L;

		// when + then
		assertThatThrownBy(() -> savingsAccountService.transferForRegularSavings(memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());

	}

	@Test
	void 적금_계좌가_존재하지_않으면_이체에_실패한다() {
		// given
		var memberId = 1L;

		// when + then
		assertThatThrownBy(() -> savingsAccountService.transferForRegularSavings(memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());
	}

	@Test
	void 요청된_인출_금액이_잔고보다_많으면_이체에_실패한다() {
		// given
		var memberId = 1L;
		var withdrawAmount = 50000L;
		var mainAccountAmount = 10000L;
		var account = mock(Account.class);
		var savingsAccount = mock(SavingsAccount.class);

		given(accountRepository.findByMemberIdWithWriteLock(anyLong())).willReturn(Optional.of(account));
		given(savingsAccountRepository.findByMemberIdWithWriteLock(anyLong())).willReturn(Optional.of(savingsAccount));
		given(savingsAccount.getWithdrawAmount()).willReturn(withdrawAmount);
		given(account.getAmount()).willReturn(mainAccountAmount);

		// when + then
		assertThatThrownBy(() -> savingsAccountService.transferForRegularSavings(memberId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.ACCOUNT_LACK_OF_AMOUNT.getMessage());
	}

	@Test
	void 자유_적금_충전에_성공한다() {
		// given
		var savingsAccount = mock(SavingsAccount.class);
		var savingsType = SavingsType.FREE;
		var transferAmount = 10000L;

		given(savingsAccountRepository.findById(anyLong())).willReturn(Optional.of(savingsAccount));
		given(savingsAccount.getSavingsType()).willReturn(savingsType);
		given(savingsAccount.getAmount()).willReturn(20000L);

		// when
		var result = savingsAccountService.transferForFreeSavings(1L, transferAmount);

		// then
		assertThat(result.saveAccountAmount()).isEqualTo(20000L);
	}

	@Test
	void 자유_적금_계좌가_없다면_충전에_실패한다() {
		// given
		var accountId = 1L;
		var transferAmount = 10000L;

		// when + then
		assertThatThrownBy(() -> savingsAccountService.transferForFreeSavings(accountId, transferAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());

	}

	@Test
	void 자유_적금이_아닌_정기_적금이라면_충전에_실패한다() {
		// given
		var transferAmount = 10000L;
		var savingsAccount = mock(SavingsAccount.class);
		var savingsType = SavingsType.REGULAR;

		given(savingsAccountRepository.findById(anyLong())).willReturn(Optional.of(savingsAccount));
		given(savingsAccount.getSavingsType()).willReturn(savingsType);

		// when + then
		assertThatThrownBy(() -> savingsAccountService.transferForFreeSavings(1L, transferAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_SAVINGS_TRANSFER.getMessage());
	}
}
