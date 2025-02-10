package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SettlementJpaRepository extends JpaRepository<Settlement, Long> {

}
