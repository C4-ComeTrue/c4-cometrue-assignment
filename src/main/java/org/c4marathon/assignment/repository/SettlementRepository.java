package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.entity.Settlement;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SettlementRepository {
	private final SettlementJpaRepository settlementJpaRepository;

	public Settlement save(Settlement settlement) {
		return settlementJpaRepository.save(settlement);
	}
}
