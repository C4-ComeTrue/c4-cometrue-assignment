package org.c4marathon.assignment.account.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // 회원의 전체 계좌 조회
    Account findByMemberId(Long memberId);

    // 회원의 메인 계좌가 존재하는지 확인
    boolean existsAccountByMemberIdAndType(Long memberId, Type type);

    // 회원의 메인 계좌 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM Account a
        WHERE a.member.id = :memberId
        """)
    Optional<Account> findByAccount(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM Account a
        WHERE a.id = :accountId
        """)
    Optional<Account> findByOtherAccount(Long accountId);
}
