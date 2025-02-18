package org.c4marathon.assignment.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.entity.TransactionStatus;
import org.c4marathon.assignment.entity.TransactionType;
import org.c4marathon.assignment.entity.TransferTransaction;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TransferTransactionRepository {
	private final TransferTransactionJpaRepository transferTransactionJpaRepository;

	public TransferTransaction save(TransferTransaction transferTransaction) {
		return transferTransactionJpaRepository.save(transferTransaction);
	}

	public int updateStatus(long transferTransactionId, TransactionStatus findStatus, TransactionStatus status) {
		return transferTransactionJpaRepository.updateStatus(transferTransactionId, findStatus, status);
	}

	public List<TransferTransaction> getTransferTransactionsByStatusAndType(TransactionStatus status,
		TransactionType type) {
		return transferTransactionJpaRepository.findAllByStatusAndType(status, type);
	}

	public List<TransferTransaction> findPendingTransactions(TransactionStatus status, TransactionType type,
		Instant targetTime) {
		return transferTransactionJpaRepository.findPendingTransactions(status, type, targetTime);
	}

	public List<TransferTransaction> findExpiredTransferTransactions(TransactionStatus status, TransactionType type,
		Instant targetTime) {
		return transferTransactionJpaRepository.findExpiredTransferTransactions(status, type, targetTime);
	}

	public Optional<TransferTransaction> findByIdAndStatus(Long transferTransactionId, TransactionStatus status) {
		return transferTransactionJpaRepository.findByIdAndStatus(transferTransactionId, status);
	}
}
