package org.c4marathon.assignment.transactional.service;

import static org.c4marathon.assignment.transactional.domain.TransactionStatus.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.global.event.transactional.TransactionCreateEvent;
import org.c4marathon.assignment.transactional.domain.Transaction;
import org.c4marathon.assignment.transactional.domain.TransactionStatus;
import org.c4marathon.assignment.transactional.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final TransactionQueryService transactionQueryService;
	private final AccountService accountService;

	public static final int PAGE_SIZE = 100;
	public static final int EXPIRATION_HOURS = 72;
	public static final int REMIND_HOURS = 24;

	@Transactional
	public void createTransaction(TransactionCreateEvent request) {

		Transaction transaction = Transaction.create(
			request.senderAccountId(),
			request.receiverAccountId(),
			request.amount(),
			request.type(),
			request.status(),
			request.sendTime()
		);
		transactionRepository.save(transaction);
	}
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void processCancelExpiredTransactions() {

		LocalDateTime expirationTime = LocalDateTime.now().minusHours(EXPIRATION_HOURS);
		Long lastId = null;

		while (true) {
			List<Transaction> transactions = transactionQueryService.findTransactionByStatusWithLastId(
				PENDING_DEPOSIT, lastId, PAGE_SIZE);

			if (transactions == null || transactions.isEmpty()) {
				break;
			}
			// transactions 데이터를 스트림을 사용해서 72시간이 지난 transactions 데이터를 필터링
			List<Transaction> expiredTransactions = transactions.stream()
				.filter(t -> t.getSendTime().isBefore(expirationTime))
				.toList();

			expiredTransactions.forEach(accountService::cancelWithdrawByExpirationTime);

			lastId = expiredTransactions.get(transactions.size() - 1).getId();
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void processRemindNotifications() {
		LocalDateTime remindTime = LocalDateTime.now().plusHours(REMIND_HOURS);
		Long lastId = null;

		while (true) {
			List<Transaction> transactions = transactionQueryService.findTransactionByStatusWithLastId(
				PENDING_DEPOSIT, lastId, PAGE_SIZE);
			if (transactions == null || transactions.isEmpty()) {
				break;
			}

			List<Transaction> remindTransactions = transactions.stream()
				.filter(t -> t.getSendTime().isAfter(remindTime))
				.toList();


			lastId = remindTransactions.get(transactions.size() - 1).getId();

		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
	public List<Transaction> findTransactionByStatusWithLastId(TransactionStatus status, Long lastId, int size) {
		if (lastId == null) {
			return transactionRepository.findTransactionByStatus(status, size);
		}
		return transactionRepository.findTransactionByStatusWithLastId(status, lastId, size);
	}
}
