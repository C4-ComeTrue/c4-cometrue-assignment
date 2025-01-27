package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
}
