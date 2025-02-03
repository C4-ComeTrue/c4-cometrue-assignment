package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

public interface AccountJpaRepository extends JpaRepository<Account, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM Account a WHERE a.id = :id")
	public Optional<Account> findByIdWithWriteLock(@Param("id") Long id);

	@Transactional
	@Modifying
	@Query(value = """
			UPDATE Account a
			SET a.balance = a.balance + :amount
			WHERE a.id = :id
		""")
	int updateBalance(@Param("id") long receiverMainAccount, @Param("amount") long amount);
}
