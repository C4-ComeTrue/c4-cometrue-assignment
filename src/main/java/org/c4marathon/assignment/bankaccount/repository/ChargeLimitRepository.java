package org.c4marathon.assignment.bankaccount.repository;

import org.c4marathon.assignment.bankaccount.entity.ChargeLimit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeLimitRepository extends JpaRepository<ChargeLimit, Long> {
}
