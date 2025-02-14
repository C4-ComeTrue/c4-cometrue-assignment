package org.c4marathon.assignment.transactional.domain.repository;

import java.util.List;

import org.c4marathon.assignment.transactional.domain.TransferTransactional;
import org.c4marathon.assignment.transactional.domain.TransactionalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionalRepository extends JpaRepository<TransferTransactional, Long> {

	//index(TransactionalStatus)
	@Query("""
		SELECT t
		FROM TransferTransactional t
		WHERE t.status = :status AND t.id > :lastId
		ORDER BY t.id
		LIMIT :size
		""")
	List<TransferTransactional> findTransactionalByStatusWithLastId(
		@Param("status") TransactionalStatus status,
		@Param("lastId") Long lastId,
		@Param("size") int size
	);

	@Query("""
		SELECT t
		FROM TransferTransactional t
		WHERE t.status = :status
		ORDER BY t.id
		LIMIT :size

		""")
	List<TransferTransactional> findTransactionalByStatus(
		@Param("status") TransactionalStatus status,
		@Param("size") int size
	);

}
