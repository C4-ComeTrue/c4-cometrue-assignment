package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.service.query.AccountQueryService;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.c4marathon.assignment.transaction.exception.InvalidTransactionStatusException;
import org.c4marathon.assignment.transaction.exception.UnauthorizedTransactionException;
import org.c4marathon.assignment.transaction.service.TransactionQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final TransactionQueryService transactionQueryService;
	private final AccountQueryService accountQueryService;

	private final MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);
	public static final int PAGE_SIZE = 1000;
	/**
	 * 송금 내역 데이터를 조회해서 출금 로직 실행
	 * status = WITHDRAW인 송금 내역을 조회해서 입금 처리를 함
	 * 입금 성공 : 송금 내역 status를 SUCCESS_DEPOSIT, 입금 한 시간을 업데이트 한다.
	 * 입금 실패 : AOP를 통해 송금 내역 status를 FAILED_DEPOSIT로 변경한다.
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void successDeposit() {
		threadPoolExecutor.init();

		LocalDateTime partitionSendTime = LocalDateTime.now();
		List<Transaction> transactions = transactionQueryService.findTransactionByStatusWithLock(
			partitionSendTime, WITHDRAW, PAGE_SIZE);

		if (transactions.isEmpty()) {
			return;
		}

		for (Transaction transactional : transactions) {
			threadPoolExecutor.execute(() -> processDeposit(transactional));
		}

		threadPoolExecutor.waitToEnd();
	}

	/**
	 * 입금 실패한 경우가 많이 없을 것이라고 생각하여 멀티 스레드 X
	 * 나중에 멀티 스레드 성능 테스트 후 결정
	 * 송금 내역 데이터를 조회해서 출금 로직 실행
	 * status = FAILED_DEPOSIT인 송금 내역을 조회해서 입금 재시도를 함
	 * 입금 성공 : 송금 내역 status를 SUCCESS_DEPOSIT, 입금 한 시간을 업데이트 한다.
	 * 입금 재시도 실패 : AOP를 통해 송금 내역 status를 CANCEL로 변경 후 송금 취소한다.
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void retryDeposit() {
		LocalDateTime partitionSendTime = LocalDateTime.now();
		List<Transaction> transactions = transactionQueryService.findTransactionByStatusWithLock(
			partitionSendTime, FAILED_DEPOSIT, PAGE_SIZE);

		if (transactions.isEmpty()) {
			return;
		}

		for (Transaction transaction : transactions) {
			processDeposit(transaction);
		}
	}

	/**
	 * 금액을 받는 사용자가 직접 확인 후 금액을 받는 비즈니스 로직
	 * @param receiverAccountNumber
	 * @param transactionalId
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void depositByReceiver(String receiverAccountNumber, Long transactionalId, LocalDateTime sendTime) {
		Transaction transaction = transactionQueryService.findTransactionByIdWithLock(transactionalId, sendTime);

		validationTransaction(receiverAccountNumber, transaction);

		Account receiverAccount = accountQueryService.findAccountWithLock(receiverAccountNumber);

		receiverAccount.deposit(transaction.getAmount());
		transaction.updateStatus(SUCCESS_DEPOSIT);
	}

	private void processDeposit(Transaction transactional) {
		String receiverAccountNumber = transactional.getReceiverAccountNumber();
		long amount = transactional.getAmount();

		Account receiverAccount = accountQueryService.findAccountWithLock(receiverAccountNumber);

		receiverAccount.deposit(amount);
		accountRepository.save(receiverAccount);

		transactional.setReceiverTime(LocalDateTime.now());
		transactional.updateStatus(SUCCESS_DEPOSIT);
		transactionRepository.save(transactional);
	}

	private static void validationTransaction(String receiverAccountNumber, Transaction transaction) {
		if (!transaction.getReceiverAccountNumber().equals(receiverAccountNumber)) {
			throw new UnauthorizedTransactionException();
		}

		if (!transaction.getStatus().equals(PENDING_DEPOSIT)) {
			throw new InvalidTransactionStatusException();
		}
	}
}
