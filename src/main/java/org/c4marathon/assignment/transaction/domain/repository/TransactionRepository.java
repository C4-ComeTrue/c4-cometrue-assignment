package org.c4marathon.assignment.transaction.domain.repository;

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

	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.senderAccountNumber = :accountNumber
		AND t.status = 'SUCCESS_DEPOSIT'
		ORDER BY t.receiverTime DESC, t.id ASC
		LIMIT :limit
		""")
	List<Transaction> findTransactionsBySenderAccount(
		@Param("accountNumber") String accountNumber,
		@Param("size") int limit
	);

	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.receiverAccountNumber = :accountNumber
		AND t.status = 'SUCCESS_DEPOSIT'
		ORDER BY t.receiverTime DESC, t.id ASC
		LIMIT :limit
		""")
	List<Transaction> findTransactionsByReceiverAccount(
		@Param("accountNumber") String accountNumber,
		@Param("size") int limit
	);

	//index(senderAccountNumber, receiverTime DESC, id ASC)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.senderAccountNumber = :accountNumber
		AND t.status = 'SUCCESS_DEPOSIT'
		AND ((t.receiverTime < :receiverTime) OR (t.receiverTime = :receiverTime AND t.id > :id))
		ORDER BY t.receiverTime DESC, t.id ASC
		LIMIT :limit
		"""
	)
	List<Transaction> findTransactionsBySenderAccountWithPageToken(
		@Param("accountNumber") String accountNumber,
		@Param("receiverTime") LocalDateTime receiverTime,
		@Param("id") Long id,
		@Param("limit") int limit
	);

	//index(receiverAccountNumber, receiverTime DESC, id ASC)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.receiverAccountNumber = :accountNumber
		AND t.status = 'SUCCESS_DEPOSIT'
		AND ((t.receiverTime < :receiverTime) OR (t.receiverTime = :receiverTime AND t.id > :id))
		ORDER BY t.receiverTime DESC, t.id ASC
		LIMIT :limit
		""")
	List<Transaction> findTransactionsByReceiverAccountWithPageToken(
		@Param("accountNumber") String accountNumber,
		@Param("receiverTime") LocalDateTime receiverTime,
		@Param("id") Long id,
		@Param("limit") int limit
	);
}
