package org.c4marathon.assignment.settlement.domain.repository;

import java.util.List;

import org.c4marathon.assignment.settlement.domain.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

	@Query("""
		SELECT sd
		FROM SettlementDetail sd
		JOIN FETCH sd.settlement s
		WHERE sd.accountId = :accountId
	""")
	List<SettlementDetail> findByAccountId(@Param("accountId") Long accountId);

}

