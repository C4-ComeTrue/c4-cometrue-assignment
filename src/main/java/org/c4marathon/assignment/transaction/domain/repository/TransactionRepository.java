package org.c4marathon.assignment.transaction.domain.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.TransactionId;
import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TransactionRepository extends JpaRepository<Transaction, TransactionId> {

	//index(TransactionalStatus, id)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.partitionSendTime = :partitonSendTime 
		AND t.status = :status 
		AND t.id > :lastId
		ORDER BY t.id
		LIMIT :size
		""")
	List<Transaction> findTransactionByStatusWithLastId(
		@Param("partitionSendTime") LocalDate partitionSendTime,
		@Param("status") TransactionStatus status,
		@Param("lastId") Long lastId,
		@Param("size") int size
	);


	//index(TransactionalStatus, id)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.partitionSendTime = :partitionSendTime
		AND t.status = :status
		ORDER BY t.id
		LIMIT :size
		""")
	List<Transaction> findTransactionByStatus(
		@Param("partitionSendTime") LocalDate partitionSendTime,
		@Param("status") TransactionStatus status,
		@Param("size") int size
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(value = """
		SELECT t.*
		FROM Transaction t
		WHERE t.send_time = :sendTime
		AND t.transaction_id = :id
		""", nativeQuery = true)
	Optional<Transaction> findTransactionalByTransactionIdWithLock(
		@Param("id") Long id,
		@Param("sendTime") LocalDateTime sendTime
	);

	//index(status, transaction_id)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(value = """
		SELECT t.*
		FROM Transaction t
		WHERE t.send_time = :sendTime
		AND t.status = :status
		ORDER BY t.transaction_id
		LIMIT :size
		""", nativeQuery = true)
	List<Transaction> findTransactionByStatusWithLock(
		@Param("sendTime") LocalDateTime sendTime,
		@Param("status") TransactionStatus status,
		@Param("size") int size
	);
}
