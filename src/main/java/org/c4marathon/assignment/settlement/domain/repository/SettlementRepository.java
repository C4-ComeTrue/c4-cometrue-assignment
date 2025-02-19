package org.c4marathon.assignment.settlement.domain.repository;

import java.util.List;

import org.c4marathon.assignment.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
	List<Settlement> findByRequestAccountNumber(String requestAccountNumber);
}
