package org.c4marathon.assignment.account.repository;

import java.util.List;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // 특정 타입의 계좌 조회
    List<Account> findByType(Type type);
}
