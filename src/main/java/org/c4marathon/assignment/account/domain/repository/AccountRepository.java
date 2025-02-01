package org.c4marathon.assignment.account.domain.repository;

import jakarta.persistence.LockModeType;
import org.c4marathon.assignment.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT a
    FROM Account a
    WHERE a.id = :id
    """)
    Optional<Account> findByIdWithLock(@Param("id") Long id);
}
