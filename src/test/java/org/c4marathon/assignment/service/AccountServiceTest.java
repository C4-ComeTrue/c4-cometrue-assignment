package org.c4marathon.assignment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.common.utils.ChargeLimitUtils;
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

}
