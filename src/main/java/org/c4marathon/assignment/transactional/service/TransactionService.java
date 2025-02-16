package org.c4marathon.assignment.transactional.service;

import java.util.List;

import org.c4marathon.assignment.global.event.transactional.TransactionCreateEvent;
import org.c4marathon.assignment.transactional.domain.Transaction;
import org.c4marathon.assignment.transactional.domain.TransactionStatus;
import org.c4marathon.assignment.transactional.domain.repository.TransactionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionalRepository transactionalRepository;

	@Transactional
	public void createTransactional(TransactionCreateEvent request) {

		Transaction transactional = Transaction.create(
			request.senderAccountId(),
			request.receiverAccountId(),
			request.amount(),
			request.type(),
			request.status(),
			request.sendTime()
		);
		transactionalRepository.save(transactional);
	}

	@Transactional(readOnly = true)
	public List<Transaction> findTransactionalByStatusWithLastId(TransactionStatus status, Long lastId, int size) {
		if (lastId == null) {
			return transactionalRepository.findTransactionalByStatus(status, size);
		}
		return transactionalRepository.findTransactionalByStatusWithLastId(status, lastId, size);
	}

}
