package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SavingsAccountJpaRepository extends JpaRepository<SavingsAccount, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM SavingsAccount s WHERE s.id = :id AND s.userId = :userId")
	Optional<SavingsAccount> findByIdAndUserIdWithWriteLock(@Param("id") Long id, @Param("userId") long userId);
}
