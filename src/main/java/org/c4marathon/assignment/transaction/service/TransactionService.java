package org.c4marathon.assignment.transaction.service;

import static org.c4marathon.assignment.global.util.Const.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.global.event.transactional.TransactionCreateEvent;
import org.c4marathon.assignment.mail.NotificationService;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final TransactionQueryService transactionQueryService;
	private final NotificationService notificationService;
	private final AccountService accountService;

	public static final int EXPIRATION_HOURS = 72;
	public static final int REMIND_HOURS = 48;

	@Transactional
	public void createTransaction(TransactionCreateEvent request) {

		Transaction transaction = Transaction.create(
			request.senderAccountNumber(),
			request.receiverAccountNumber(),
			request.amount(),
			request.type(),
			request.status(),
			request.sendTime()
		);
		transactionRepository.save(transaction);
	}

	/**
	 * 72시간이 지나도록 수령인이 송금을 받지 않으면 송금을 취소시키는 로직
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void processCancelExpiredTransactions() {
		LocalDateTime expirationTime = LocalDateTime.now().minusHours(EXPIRATION_HOURS);

		List<Transaction> transactions = transactionQueryService.findTransactionByStatusWithLock(
			expirationTime, PENDING_DEPOSIT, PAGE_SIZE);

		// transactions 데이터를 스트림을 사용해서 72시간이 지난 transactions 데이터를 필터링
		List<Transaction> expiredTransactions = transactions.stream()
			.filter(t -> t.getSendTime().isBefore(expirationTime))
			.toList();

		expiredTransactions.forEach(accountService::cancelWithdrawByExpirationTime);
	}

/*	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void processRemindNotifications() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime remindTime = now.minusHours(REMIND_HOURS);
		LocalDateTime expirationTime = now.minusHours(EXPIRATION_HOURS);



		while (true) {
			List<Transaction> transactions = transactionQueryService.findTransactionByStatusWithLock(
				PENDING_DEPOSIT, PAGE_SIZE);
			if (transactions == null || transactions.isEmpty()) {
				break;
			}

			List<Transaction> remindTransactions = transactions.stream()
				.filter(t -> {
					LocalDateTime sendTime = t.getSendTime();
					return sendTime.isBefore(remindTime) && sendTime.isAfter(expirationTime);
				})
				.toList();

			remindTransactions.forEach(notificationService::sendRemindNotification);

			lastId = transactions.get(transactions.size() - 1).getId();
		}
	}*/
}
