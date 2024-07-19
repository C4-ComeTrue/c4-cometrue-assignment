package org.c4marathon.assignment.domain.account.repository;

import jakarta.persistence.LockModeType;
import org.c4marathon.assignment.domain.account.entity.SavingAccount;
import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {

    List<SavingAccount> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM SavingAccount a WHERE a.id = :id")
    Optional<SavingAccount> findByIdWithWriteLock(@Param("id") Long id);
}
