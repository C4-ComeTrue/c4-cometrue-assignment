package org.c4marathon.assignment.account.domain.repository;

import java.util.Optional;

import org.c4marathon.assignment.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

	//index(accountNumber)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT a
		FROM Account a
		WHERE a.accountNumber = :accountNumber
		""")
	Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);

	Optional<Account> findByAccountNumber(String accountNumber);
}
