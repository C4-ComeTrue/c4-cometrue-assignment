package org.c4marathon.assignment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.common.event.TransferEvent;
import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.domain.entity.TransferLog;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.c4marathon.assignment.repository.TransferLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	AccountRepository accountRepository;

	@Mock
	MemberRepository memberRepository;

	@Mock
	ChargeService chargeService;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	TransferLogRepository transferLogRepository;

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
		long accountId = 1L;
		long transferAccountId = 2L;
		String transferAccountNumber = "11-22";
		long transferAmount = 1000L;

		// A 계좌 (송금자)
		Account senderAccount = mock(Account.class);
		given(accountRepository.findById(accountId)).willReturn(Optional.of(senderAccount));
		given(senderAccount.isAmountLackToWithDraw(transferAmount)).willReturn(false);
		given(accountRepository.existsByAccountNumber(transferAccountNumber)).willReturn(true);
		given(accountRepository.withdraw(accountId, transferAmount)).willReturn(1);
		given(accountRepository.findAmount(accountId)).willReturn(9000L);

		// B 계좌 (수신자)
		Account receiverAccount = mock(Account.class);
		given(accountRepository.findByAccountNumberWithWriteLock(transferAccountNumber))
			.willReturn(Optional.of(receiverAccount));
		given(receiverAccount.getId()).willReturn(transferAccountId);

		// 이벤트 객체
		TransferEvent transferEvent = new TransferEvent(this, null, accountId, transferAccountNumber, transferAmount);

		// when
		accountService.transferAsync(accountId, transferAccountNumber, transferAmount);
		accountService.transferPostProcess(transferEvent); // 실제 이벤트 리스너 수동 호출

		// then
		verify(accountRepository).withdraw(accountId, transferAmount);
		verify(eventPublisher).publishEvent(any(TransferEvent.class));
		verify(accountRepository).deposit(transferAccountId, transferAmount);
		verify(transferLogRepository).save(any(TransferLog.class));
	}


	@Test
	void 잔액이_부족하다면_자동_충전이_발생한다() {
		var accountId = 1L;
		var transferAccountNumber = "11-22";
		var transferAmount = 1000L;
		var account = mock(Account.class);

		given(account.isAmountLackToWithDraw(anyLong())).willReturn(true);

		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(accountRepository.withdraw(accountId, transferAmount)).willReturn(1);
		given(accountRepository.existsByAccountNumber(anyString())).willReturn(true);

		// when
		accountService.transferAsync(accountId, transferAccountNumber, transferAmount);

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
		assertThatThrownBy(() -> accountService.transferAsync(accountId, transferAccountNumber, transferAmount))
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
		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(accountRepository.existsByAccountNumber(anyString())).willReturn(false);

		assertThatThrownBy(() -> accountService.transferAsync(accountId, transferAccountNumber, transferAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.INVALID_ACCOUNT.getMessage());
	}

	@Test
	void 송금_도중_잔액이_부족해진다면_실패한다() {
		var accountId = 1L;
		var transferAccountNumber = "11-22";
		var transferAmount = 10000L;
		var account = mock(Account.class);

		// when
		given(account.isAmountLackToWithDraw(anyLong())).willReturn(false);

		given(accountRepository.findById(anyLong())).willReturn(Optional.of(account));
		given(accountRepository.existsByAccountNumber(anyString())).willReturn(true);
		given(accountRepository.withdraw(accountId, transferAmount)).willReturn(0);

		assertThatThrownBy(() -> accountService.transferAsync(accountId, transferAccountNumber, transferAmount))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.ACCOUNT_LACK_OF_AMOUNT.getMessage());
	}
}
