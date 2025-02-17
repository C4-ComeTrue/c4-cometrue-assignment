package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository  extends JpaRepository<Transaction, Long> {
}
