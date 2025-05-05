package org.c4marathon.assignment.adjustment.repository;

import java.util.List;

import org.c4marathon.assignment.adjustment.entity.AdjustTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdjustTargetRepository extends JpaRepository<AdjustTarget, Long> {

    // 정산 대상자 조회
    @Query("""
        SELECT at
        FROM AdjustTarget at
        WHERE at.adjust = :adjustId
    """)
    List<AdjustTarget> findByAdjustId(Long adjustId);
}
