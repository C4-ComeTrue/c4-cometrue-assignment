package org.c4marathon.assignment.domain.coupon.repository;

import org.c4marathon.assignment.domain.coupon.entity.FailedCouponLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedCouponLogRepository extends JpaRepository<FailedCouponLog, Long> {
}
