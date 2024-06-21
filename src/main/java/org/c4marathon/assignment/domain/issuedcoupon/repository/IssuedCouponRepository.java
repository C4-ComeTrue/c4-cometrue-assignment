package org.c4marathon.assignment.domain.issuedcoupon.repository;

import org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {

	@Query(
		value = """
				select count(*) from issued_coupon_tbl ic
				join coupon_tbl c on c.coupon_id = ic.coupon_id
				where c.event_id = :eventId and ic.consumer_id = :consumerId
			""",
		nativeQuery = true
	)
	Long countByConsumerIdAndEventId(@Param("consumerId") Long consumerId, @Param("eventId") Long eventId);
}
