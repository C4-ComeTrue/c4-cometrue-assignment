package org.c4marathon.assignment.domain.issuedcoupon.repository;

import org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {

	@Query(value = """
		select 1
		from issued_coupon_tbl ic
		         join coupon_tbl c on c.coupon_id = ic.coupon_id
		where c.event_id = :eventId
		  	and ic.consumer_id = :consumerId
		limit 1
		""",
		nativeQuery = true
	)
	Long existsByConsumerIdCouponId_EventId(Long consumerId, Long eventId);

	@Modifying
	@Query(value = """
			delete
		 	from issued_coupon_tbl ic
			where ic.coupon_id = :couponId
		""", nativeQuery = true)
	void deleteByCouponId(Long usedCouponId);
}
