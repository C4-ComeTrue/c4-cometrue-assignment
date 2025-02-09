package org.c4marathon.assignment.settlement.entity.repository;

import java.util.List;

import org.c4marathon.assignment.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
	List<Settlement> findByRequestAccountId(Long requestAccountId);
}
