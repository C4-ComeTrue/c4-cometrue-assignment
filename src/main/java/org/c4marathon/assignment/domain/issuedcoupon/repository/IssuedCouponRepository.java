package org.c4marathon.assignment.domain.issuedcoupon.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.issuedcoupon.entity.IssuedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
	Long existsByConsumerIdCouponId_EventId(@Param("consumerId") Long consumerId, @Param("eventId") Long eventId);

	@Modifying
	@Query(value = """
			delete
		 	from issued_coupon_tbl ic
			where ic.coupon_id = :couponId
		""", nativeQuery = true)
	void deleteByCouponId(@Param("couponId") Long couponId);

	@Query(value = """
		select c.coupon_id
		from issued_coupon_tbl ic
		         join coupon_tbl c on c.coupon_id = ic.coupon_id
		where c.expired_time < :now
		""", nativeQuery = true)
	List<Long> findExpiredCouponId(@Param("now") LocalDateTime now);
}
