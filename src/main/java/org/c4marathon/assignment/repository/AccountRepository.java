package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ac from Account ac where ac.id = :id")
	Optional<Account> findByIdWithWriteLock(@Param("id") long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ac from Account ac where ac.member.id = :id")
	Optional<Account> findByMemberIdWithWriteLock(@Param("id") long memberId);
}
