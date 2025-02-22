package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.dto.TransactionRemindInfo;
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
		SELECT id, receiver_account_number as receiverAccountNumber, balance, deadline
		FROM transaction
		WHERE id > :cursorId AND deadline >= :cursorTime AND deadline <= :endTime
		LIMIT :limit
	""", nativeQuery = true)
	List<TransactionRemindInfo> findAllRemindInfoByCursor(long cursorId, LocalDateTime cursorTime, LocalDateTime endTime, int limit);
}
