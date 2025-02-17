package org.c4marathon.assignment.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.entity.TransactionStatus;
import org.c4marathon.assignment.entity.TransactionType;
import org.c4marathon.assignment.entity.TransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TransferTransactionJpaRepository extends JpaRepository<TransferTransaction, Long> {
	@Transactional
	@Modifying
	@Query(value = """
			UPDATE TransferTransaction t
			SET t.status = :status
			WHERE t.id = :id AND t.status = :findStatus
		""")
	int updateStatus(@Param("id") long transferTransactionId, @Param("findStatus") TransactionStatus findStatus,
		@Param("status") TransactionStatus status);

	List<TransferTransaction> findAllByStatusAndType(TransactionStatus status, TransactionType type);

	@Query(value = """
			SELECT t FROM TransferTransaction t
			WHERE t.status = :status
			AND t.type = :type
			AND t.createDate = :targetTime
		""")
	List<TransferTransaction> findPendingTransactions(@Param("status") TransactionStatus status,
		@Param("type") TransactionType type, @Param("targetTime") Instant targetTime);

	@Query(value = """
			SELECT t FROM TransferTransaction t
			WHERE t.status = :status
			AND t.type = :type
			AND t.createDate < :targetTime
		""")
	List<TransferTransaction> findExpiredTransferTransactions(TransactionStatus status, TransactionType type, Instant targetTime);

	Optional<TransferTransaction> findByIdAndStatus(Long transferTransactionId, TransactionStatus status);
}
