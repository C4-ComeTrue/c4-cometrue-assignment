package org.c4marathon.assignment.adjustment.repository;

import java.util.Optional;

import org.c4marathon.assignment.adjustment.entity.Adjust;
import org.c4marathon.assignment.util.common.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AdjustRepository extends JpaRepository<Adjust, Long> {

    // 상태별 정산 조회
    @Query("""
        SELECT a 
        FROM Adjust a
        WHERE a.id = :id
            AND a.status = :status
    """)
    Optional<Adjust> findByStatus(Long id, Status status);

    // 정산 현황 조회

    // 정산 상태 변경
    @Modifying
    @Query("""
        UPDATE Adjust a
        SET a.status = :status
            , a.updated = NOW()
        WHERE a.id = :id
    """)
    void updateStatus(Status status, Long id);
}
