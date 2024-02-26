package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {

	Optional<SavingsAccount> findByMemberId(long memberId);

	@Modifying
	@Query("update SavingsAccount ac "
		+ "set ac.amount = ac.amount + :chargeAmount where ac.id = :id")
	void charge(@Param("id") long id, @Param("chargeAmount") long chargeAmount);

}
