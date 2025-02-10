package org.c4marathon.assignment.account.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.util.Const.*;

import java.util.Optional;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AccountRepositoryTest extends IntegrationTestSupport {

	@Autowired
	private AccountRepository accountRepository;

	@DisplayName("Account Id를 통해 Account를 조회한다. ")
	@Test
	@Transactional
	void findAccountByAccountId() throws Exception {
	    // given
		Account account = Account.create(DEFAULT_BALANCE);
		accountRepository.save(account);

	    // when
		Optional<Account> findAccount = accountRepository.findByIdWithLock(account.getId());

		// then

		assertThat(findAccount.get())
			.extracting("money", "chargeLimit")
			.contains(0L, CHARGE_LIMIT);
	}
}