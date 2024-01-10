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

	@Query("select "
		+ "sum(case WHEN p.valueType = 'CHARGE' THEN p.value ELSE -p.value END) "
		+ "from Payment p where p.member = :member")
	int currentBalance(Member member);

}
