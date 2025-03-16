package org.c4marathon.assignment.account.service.query;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountQueryService {
	private final AccountRepository accountRepository;

	public Account findAccountWithLock(String accountNumber) {
		return accountRepository.findByAccountNumberWithLock(accountNumber)
			.orElseThrow(NotFoundAccountException::new);
	}

	public void findAccount(String accountNumber) {
		accountRepository.findByAccountNumber(accountNumber).orElseThrow(NotFoundAccountException::new);
	}

}
