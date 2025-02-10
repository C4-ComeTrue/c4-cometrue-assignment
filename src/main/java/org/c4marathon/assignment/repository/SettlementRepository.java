package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
