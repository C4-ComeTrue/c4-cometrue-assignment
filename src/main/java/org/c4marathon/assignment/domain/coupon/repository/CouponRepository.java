package org.c4marathon.assignment.domain.coupon.repository;

import org.c4marathon.assignment.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

	boolean existsByName(String name);
}
