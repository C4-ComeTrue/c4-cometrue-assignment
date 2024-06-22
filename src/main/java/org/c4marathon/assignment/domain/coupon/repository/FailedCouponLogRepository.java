package org.c4marathon.assignment.domain.coupon.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.coupon.entity.FailedCouponLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FailedCouponLogRepository extends JpaRepository<FailedCouponLog, Long> {

	@Query(value = """
			select * from failed_coupon_log_tbl fcl limit 1
		""", nativeQuery = true)
	Optional<FailedCouponLog> findFirst();

	boolean existsByCouponId(Long couponId);
}
