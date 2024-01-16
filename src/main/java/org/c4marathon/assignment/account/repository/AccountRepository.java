package org.c4marathon.assignment.account.repository;

import java.util.List;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // 특정 타입의 계좌 조회
    List<Account> findByType(Type type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a "
        + " WHERE a.member.id = :memberId"
        + " AND a.type = :type")
    Account findByAccount(Long memberId, Type type);
}
