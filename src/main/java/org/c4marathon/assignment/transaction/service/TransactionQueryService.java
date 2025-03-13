package org.c4marathon.assignment.transaction.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.c4marathon.assignment.global.model.PageInfo;
import org.c4marathon.assignment.global.util.PageTokenUtil;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.TransactionSearchOption;
import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.c4marathon.assignment.transaction.dto.TransactionResponse;
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

	public PageInfo<TransactionResponse> findTransactionsWithoutPageToken(
		String accountNumber,
		TransactionSearchOption option,
		int count
	) {
		var data = switch (option) {
			case SENDER -> transactionRepository.findTransactionsBySenderAccount(accountNumber, count + 1);
			case RECEIVER -> transactionRepository.findTransactionsByReceiverAccount(accountNumber, count + 1);
			case ALL -> mergeAllOptions(
				transactionRepository.findTransactionsBySenderAccount(accountNumber, count + 1),
				transactionRepository.findTransactionsByReceiverAccount(accountNumber, count + 1),
				count + 1
			);
		};

		List<TransactionResponse> responses = data.stream()
			.map(TransactionResponse::from)
			.toList();

		return PageInfo.of(responses, count, TransactionResponse::receiverTime, TransactionResponse::transactionId);
	}

	public PageInfo<TransactionResponse> findTransactionsWithPageToken(
		String accountNumber,
		String pageToken,
		TransactionSearchOption option,
		int count
	) {
		var pageData = PageTokenUtil.decodePageToken(pageToken, LocalDateTime.class, Long.class);
		var receiverTime = pageData.getLeft();
		var transactionId = pageData.getRight();

		var data = switch (option) {
			case SENDER -> transactionRepository.findTransactionsBySenderAccountWithPageToken(accountNumber, receiverTime, transactionId, count + 1);
			case RECEIVER -> transactionRepository.findTransactionsByReceiverAccountWithPageToken(accountNumber, receiverTime, transactionId, count + 1);
			case ALL -> mergeAllOptions(
				transactionRepository.findTransactionsBySenderAccountWithPageToken(accountNumber, receiverTime, transactionId, count + 1),
				transactionRepository.findTransactionsByReceiverAccountWithPageToken(accountNumber, receiverTime, transactionId, count + 1),
				count + 1
			);
		};

		List<TransactionResponse> responses = data.stream()
			.map(TransactionResponse::from)
			.toList();

		return PageInfo.of(responses, count, TransactionResponse::receiverTime, TransactionResponse::transactionId);
	}

	private List<Transaction> mergeAllOptions(
		List<Transaction> senderResult,
		List<Transaction> receiverResult,
		int count
	) {
		return ListUtils.union(senderResult, receiverResult).stream()
			.sorted(
				Comparator.comparing(Transaction::getReceiverTime).reversed()
					.thenComparing(Transaction::getId)
			)
			.limit(count)
			.toList();
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
