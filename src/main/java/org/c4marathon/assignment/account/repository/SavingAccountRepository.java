package org.c4marathon.assignment.account.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long>  {

    List<SavingAccount> findByMemberId(Long memberId);

    // 회원의 적금 계좌 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT sa FROM SavingAccount sa
        WHERE sa.member.id = :memberId
        AND sa.id = :id
        """)
    Optional<SavingAccount> findBySavingAccount(Long memberId, Long id);

    @Modifying
    @Query("""
        UPDATE SavingAccount sa SET sa.balance = sa.balance + :balance
        WHERE sa.id = :id
        """)
    void transferSavingAccount(Long id, Long balance);
}
