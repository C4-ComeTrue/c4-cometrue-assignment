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

	@Query(value = """
		SELECT id, sender_account_number as senderAccountNumber, receiver_account_number as receiverAccountNumber, balance, deadline
		FROM transaction
		WHERE id > :cursorId AND deadline >= :cursorTime AND deadline <= :endTime AND state = :state
		LIMIT :limit
	""", nativeQuery = true)
	List<TransactionInfo> findAllInfoBy(long cursorId, LocalDateTime cursorTime, LocalDateTime endTime, TransactionState state, int limit);
}
