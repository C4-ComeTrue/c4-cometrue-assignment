package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.dto.TransactionInfo;
import org.c4marathon.assignment.domain.type.TransactionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	@Modifying
	@Query("UPDATE Transaction SET state = :updateState WHERE id = :transactionId AND state = :preState")
	int updateState(@Param("transactionId") long transactionId,
		@Param("preState") TransactionState preState,
		@Param("updateState") TransactionState updateState);

	@Modifying
	@Query("UPDATE Transaction SET state = :updateState WHERE id IN :transactionIds AND state = :preState")
	int updateState(@Param("transactionIds") List<Long> transactionIds,
		@Param("preState") TransactionState preState,
		@Param("updateState") TransactionState updateState);

	@Query(value = """
		SELECT id, sender_account_number as senderAccountNumber, receiver_account_number as receiverAccountNumber, balance, deadline
		FROM transaction
		WHERE id > :cursorId AND deadline >= :cursorTime AND deadline <= :endTime AND state = :state
		LIMIT :limit
	""", nativeQuery = true)
	List<TransactionInfo> findAllInfoBy(@Param("cursorId") long cursorId, @Param("cursorTime") LocalDateTime cursorTime,
		@Param("endTime") LocalDateTime endTime, @Param("state") String state, @Param("limit") int limit);

	@Query(value = """
		SELECT id, sender_account_number as senderAccountNumber, receiver_account_number as receiverAccountNumber, balance, deadline
		FROM transaction
		WHERE deadline <= :endTime AND state = :state
		LIMIT :limit FOR UPDATE
	""", nativeQuery = true)
	List<TransactionInfo> findAllAutoCancelInfoWithXLockBy(@Param("endTime") LocalDateTime endTime, @Param("state") String state, @Param("limit") int limit);

	@Query(value = """
		SELECT id, sender_account_number as senderAccountNumber, receiver_account_number as receiverAccountNumber, balance, deadline
		FROM transaction
		WHERE id > :cursorId AND deadline <= :endTime AND state = :state
		LIMIT :limit FOR UPDATE
	""", nativeQuery = true)
	List<TransactionInfo> findAllAutoCancelInfoWithXLockBy(@Param("cursorId") long cursorId, @Param("endTime") LocalDateTime endTime,
		@Param("state") String state, @Param("limit") int limit);
}
