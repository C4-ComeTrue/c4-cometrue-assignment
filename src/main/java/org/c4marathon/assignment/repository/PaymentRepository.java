package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	@Query("select p from Payment p where p.member = :member")
	List<Payment> findByMemberId(Member member);

	@Query("select COALESCE(SUM(p.value), 0) from Payment p where p.member = :member "
		+ "AND (p.valueType = 'CHARGE' OR p.valueType = 'COMMISSION')")
	Integer totalCharged(Member member);

	@Query("select COALESCE(SUM(p.value), 0) from Payment p where p.member = :member "
		+ "AND (p.valueType = 'DISCHARGE' OR p.valueType = 'REFUND')")
	Integer totalDischarged(Member member);

}
