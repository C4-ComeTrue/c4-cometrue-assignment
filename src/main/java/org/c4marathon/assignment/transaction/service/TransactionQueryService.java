package org.c4marathon.assignment.transaction.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.c4marathon.assignment.transaction.exception.NotFoundTransactionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {
	private final TransactionRepository transactionRepository;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public Transaction findTransactionByIdWithLock(Long transactionId, LocalDateTime sendTime) {
		return transactionRepository.findTransactionalByTransactionIdWithLock(transactionId, sendTime)
			.orElseThrow(NotFoundTransactionException::new);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public List<Transaction> findTransactionByStatusWithLock(
		LocalDateTime sendTime,
		TransactionStatus status,
		int size
	) {
		return transactionRepository.findTransactionByStatusWithLock(sendTime, status, size);
	}
/*
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public List<Transaction> findTransactionByStatusWithLastId(
		TransactionStatus status,
		LocalDate partitionSendTime,
		Long lastId,
		int size
	) {
		if (lastId == null) {
			return transactionRepository.findTransactionByStatus(partitionSendTime, status, size);
		}
		return transactionRepository.findTransactionByStatusWithLastId(partitionSendTime, status, lastId, size);
	}*/
}
