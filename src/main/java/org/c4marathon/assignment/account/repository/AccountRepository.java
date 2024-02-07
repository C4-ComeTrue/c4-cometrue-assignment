package org.c4marathon.assignment.account.repository;

import java.util.List;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // 회원의 전체 계좌 조회
    List<Account> findByMember(Member member);

    // 회원의 특정 계좌 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM Account a
        WHERE a.member.id = :memberId
        AND a.id = :id
        """)
    Account findByAccount(Long memberId, Long id);

    // 회원의 메인 계좌 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM Account a
        WHERE a.member.id = :memberId
        AND a.type = 'REGULAR_ACCOUNT'
        """)
    Account findByRegularAccount(Long memberId);
}
