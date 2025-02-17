package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementJpaRepository extends JpaRepository<Settlement, Long> {

}
