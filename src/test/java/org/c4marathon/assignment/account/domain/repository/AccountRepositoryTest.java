package org.c4marathon.assignment.account.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.util.Const.*;

import java.util.Optional;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.global.util.AccountNumberUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AccountRepositoryTest extends IntegrationTestSupport {

	@Autowired
	private AccountRepository accountRepository;

	@Transactional
	@DisplayName("AccountNumber 를 통해 Account를 조회한다. ")
	@Test
	void findAccountByAccountId() throws Exception {
	    // given
		String accountNumber = generateAccountNumber();
		Account account = Account.create(accountNumber, DEFAULT_BALANCE);
		accountRepository.save(account);

	    // when
		Optional<Account> findAccount = accountRepository.findByAccountNumberWithLock(account.getAccountNumber());

		// then

		assertThat(findAccount.get())
			.extracting("accountNumber", "money", "chargeLimit")
			.contains(accountNumber, 0L, CHARGE_LIMIT);
	}

	private String generateAccountNumber() {
		return AccountNumberUtil.generateAccountNumber("3333");
	}
}