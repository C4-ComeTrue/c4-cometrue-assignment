package org.c4marathon.assignment.bankaccount.repository;

import java.util.Optional;

import org.c4marathon.assignment.bankaccount.entity.ChargeLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ChargeLimitRepository extends JpaRepository<ChargeLimit, Long> {
	@Modifying
	@Query("""
		update ChargeLimit cl
		set cl.spareMoney = cl.limitMoney,
		cl.chargeCheck = false
		where cl.chargeCheck = true
		""")
	void bulkSpareMoneyInit();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select cl from ChargeLimit cl where cl.limitPk = :limitPk")
	Optional<ChargeLimit> findByPkForUpdate(@Param("limitPk") long limitPk);
}
