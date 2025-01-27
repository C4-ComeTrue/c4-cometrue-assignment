package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsAccountJpaRepository extends JpaRepository<SavingsAccount, Long> {
}
