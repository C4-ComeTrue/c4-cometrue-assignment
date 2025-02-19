package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transaction.domain.TransactionType.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.global.util.AccountNumberUtil;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.c4marathon.assignment.transaction.exception.InvalidTransactionStatusException;
import org.c4marathon.assignment.transaction.exception.NotFoundTransactionException;
import org.c4marathon.assignment.transaction.exception.UnauthorizedTransactionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
		String senderAccountNumber = generateAccountNumber();
		String receiverAccountNumber = generateAccountNumber();
		Account senderAccount = createAccount(senderAccountNumber, 10000L);
		Account receiverAccount = createAccount(receiverAccountNumber, 20000L);

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
		String senderAccountNumber = generateAccountNumber();
		String receiverAccountNumber = generateAccountNumber();
		Account senderAccount = createAccount(senderAccountNumber,10000L);
		Account receiverAccount = createAccount(receiverAccountNumber, 20000L);

		Transaction transactional = createTransactional(senderAccount, receiverAccount, 1000L);

		// when
		depositService.failedDeposit(transactional);

		// then
		Account updatedReceiverAccount = accountRepository.findById(receiverAccount.getId())
			.orElseThrow(NotFoundAccountException::new);

		assertThat(updatedReceiverAccount.getMoney()).isEqualTo(21000L);
	}

	@Transactional
	@DisplayName("송금을 받은 사용자가 직접 확인 후 금액을 수령한다.")
	@Test
	void depositByReceiver() {
	    // given
		String senderAccountNumber = generateAccountNumber();
		String receiverAccountNumber = generateAccountNumber();
		Account receiverAccount = createAccount(receiverAccountNumber, 10000L);
		Transaction transaction = Transaction.create(senderAccountNumber, receiverAccount.getAccountNumber(), 2000L, PENDING_TRANSFER,
			PENDING_DEPOSIT, LocalDateTime.now().minusHours(1));
		transactionRepository.save(transaction);

	    // when
		depositService.depositByReceiver(receiverAccount.getAccountNumber(), transaction.getId());

	    // then
		Account updatedReceiverAccount = accountRepository.findById(receiverAccount.getId())
			.orElseThrow(NotFoundAccountException::new);
		Transaction updatedTransaction = transactionRepository.findById(transaction.getId())
			.orElseThrow(NotFoundTransactionException::new);

		assertThat(updatedReceiverAccount.getMoney()).isEqualTo(12000L);
		assertThat(updatedTransaction.getStatus()).isEqualTo(SUCCESS_DEPOSIT);
	}

	@DisplayName("송금 내역 상태가 PENDING_DEPOSIT이 아닌 송금 내역을 수령 시 예외가 발생한다.")
	@Test
	void depositByReceiverWithInvalidTransactionStatus() {
	    // given
		String senderAccountNumber = generateAccountNumber();
		String receiverAccountNumber = generateAccountNumber();
		Account receiverAccount = createAccount(receiverAccountNumber, 10000L);
		Transaction transaction = Transaction.create(senderAccountNumber, receiverAccount.getAccountNumber(), 2000L, PENDING_TRANSFER,
			SUCCESS_DEPOSIT, LocalDateTime.now().minusHours(1));
		transactionRepository.save(transaction);

	    // when // then
		assertThatThrownBy(() -> depositService.depositByReceiver(receiverAccount.getAccountNumber(), transaction.getId()))
			.isInstanceOf(InvalidTransactionStatusException.class);
	}

	@DisplayName("송금 내역 상태가 PENDING_DEPOSIT이 아닌 송금 내역을 수령 시 예외가 발생한다.")
	@Test
	void depositByReceiverWithUnauthorizedTransaction() {
		// given
		String senderAccountNumber = generateAccountNumber();
		String receiverAccountNumber = generateAccountNumber();
		String otherAccountNumber = generateAccountNumber();
		Account receiverAccount = createAccount(receiverAccountNumber, 10000L);
		Account otherReceiverAccount = createAccount(otherAccountNumber, 10000L);
		Transaction transaction = Transaction.create(senderAccountNumber, receiverAccount.getAccountNumber(), 2000L, PENDING_TRANSFER,
			PENDING_DEPOSIT, LocalDateTime.now().minusHours(1));
		transactionRepository.save(transaction);

		// when // then
		assertThatThrownBy(() -> depositService.depositByReceiver(otherReceiverAccount.getAccountNumber(), transaction.getId()))
			.isInstanceOf(UnauthorizedTransactionException.class);
	}

	private Account createAccount(String accountNumber, long money) {
		Account account = Account.create(accountNumber, money);
		accountRepository.save(account);
		return account;
	}

	private Transaction createTransactional(Account senderAccount, Account receiverAccount, long amount) {
		Transaction transactional = Transaction.create(senderAccount.getAccountNumber(),
			receiverAccount.getAccountNumber(), amount, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now());
		transactionRepository.save(transactional);

		return transactional;
	}

	private String generateAccountNumber() {
		return AccountNumberUtil.generateAccountNumber("3333");
	}
}
