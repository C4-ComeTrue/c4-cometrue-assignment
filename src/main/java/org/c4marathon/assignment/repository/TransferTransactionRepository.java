package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.entity.TransactionStatus;
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

	public List<TransferTransaction> getTransferTransactionsByStatus(TransactionStatus status) {
		return transferTransactionJpaRepository.findAllByStatus(status);
	}

}
