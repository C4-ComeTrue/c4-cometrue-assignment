package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.entity.TransactionStatus;
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
}
