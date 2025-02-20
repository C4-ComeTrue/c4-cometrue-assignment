package org.c4marathon.assignment.domain;

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
}
