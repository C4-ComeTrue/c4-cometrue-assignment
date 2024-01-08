package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<Sales, Long> {
}
