package org.c4marathon.assignment.bankaccount.repository;

import org.c4marathon.assignment.bankaccount.entity.ChargeLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChargeLimitRepository extends JpaRepository<ChargeLimit, Long> {
	@Modifying
	@Query("""
		update ChargeLimit cl
		set cl.spareMoney = cl.limitMoney,
		cl.chargeCheck = false
		where cl.chargeCheck = true
		""")
	void bulkSpareMoneyInit();
}
