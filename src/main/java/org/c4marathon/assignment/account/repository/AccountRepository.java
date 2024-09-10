package org.c4marathon.assignment.account.repository;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional findByAccountNum(Long accountNum);

    Optional findByAccountPw(int accountPw);

    Optional<Account> findByUser_IdAndType(Long userId, AccountType type);

}
