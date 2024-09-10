package org.c4marathon.assignment.account.repository;

import org.c4marathon.assignment.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Long, Account> {
}
