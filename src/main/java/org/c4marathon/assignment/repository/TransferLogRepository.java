package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.entity.TransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferLogRepository extends JpaRepository<TransferLog, Long> {
}
