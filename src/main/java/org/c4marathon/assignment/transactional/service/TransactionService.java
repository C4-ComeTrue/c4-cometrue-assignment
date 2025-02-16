package org.c4marathon.assignment.transactional.service;

import java.util.List;

import org.c4marathon.assignment.global.event.transactional.TransactionCreateEvent;
import org.c4marathon.assignment.transactional.domain.Transaction;
import org.c4marathon.assignment.transactional.domain.TransactionStatus;
import org.c4marathon.assignment.transactional.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;

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

	@Transactional(readOnly = true)
	public List<Transaction> findTransactionByStatusWithLastId(TransactionStatus status, Long lastId, int size) {
		if (lastId == null) {
			return transactionRepository.findTransactionByStatus(status, size);
		}
		return transactionRepository.findTransactionByStatusWithLastId(status, lastId, size);
	}

}
