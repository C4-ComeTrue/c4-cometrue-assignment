package org.c4marathon.assignment.bankaccount.repository;

import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
}
