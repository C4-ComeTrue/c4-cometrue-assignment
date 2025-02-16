package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.transactional.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transactional.domain.TransactionType.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.transactional.domain.Transaction;
import org.c4marathon.assignment.transactional.domain.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DepositServiceTest extends IntegrationTestSupport {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private DepositService depositService;

	@AfterEach
	void tearDown() {
		accountRepository.deleteAllInBatch();
		transactionRepository.deleteAllInBatch();
	}

	@DisplayName("입금이 성공한다.")
	@Test
	void successDeposit() {
		// given
		Account senderAccount = createAccount(10000L);
		Account receiverAccount = createAccount(20000L);

		Transaction transactional = createTransactional(senderAccount, receiverAccount, 1000L);

		// when
		depositService.successDeposit(transactional);

		// then
		Account updatedReceiverAccount = accountRepository.findById(receiverAccount.getId())
			.orElseThrow(NotFoundAccountException::new);

		assertThat(updatedReceiverAccount.getMoney()).isEqualTo(21000L);
	}

	@DisplayName("입금 재시도를 성공한다.")
	@Test
	void failedDeposit() {
		// given
		Account senderAccount = createAccount(10000L);
		Account receiverAccount = createAccount(20000L);

		Transaction transactional = createTransactional(senderAccount, receiverAccount, 1000L);

		// when
		depositService.failedDeposit(transactional);

		// then
		Account updatedReceiverAccount = accountRepository.findById(receiverAccount.getId())
			.orElseThrow(NotFoundAccountException::new);

		assertThat(updatedReceiverAccount.getMoney()).isEqualTo(21000L);
	}

	private Account createAccount(long money) {
		Account account = Account.create(money);
		accountRepository.save(account);
		return account;
	}

	private Transaction createTransactional(Account senderAccount, Account receiverAccount, long amount) {
		Transaction transactional = Transaction.create(senderAccount.getId(),
			receiverAccount.getId(), amount, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now());
		transactionRepository.save(transactional);

		return transactional;
	}
}
