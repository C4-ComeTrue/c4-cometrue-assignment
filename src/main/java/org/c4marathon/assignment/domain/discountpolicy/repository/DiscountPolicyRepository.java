package org.c4marathon.assignment.domain.discountpolicy.repository;

import org.c4marathon.assignment.domain.discountpolicy.entity.DiscountPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Integer> {

	boolean existsByName(String name);
}
