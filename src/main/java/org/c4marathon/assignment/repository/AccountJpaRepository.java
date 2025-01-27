package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<Account, Long> {
}
