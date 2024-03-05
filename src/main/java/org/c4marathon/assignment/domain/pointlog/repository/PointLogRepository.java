package org.c4marathon.assignment.domain.pointlog.repository;

import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
}
