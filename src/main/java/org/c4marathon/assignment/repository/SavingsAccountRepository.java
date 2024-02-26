package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select sac from SavingsAccount sac where sac.member.id = :id")
	Optional<SavingsAccount> findByMemberIdWithWriteLock(@Param("id") long memberId);

	// Optional<SavingsAccount> findByMemberId(long memberId);
	//
	// @Modifying
	// @Query("update SavingsAccount ac "
	// 	+ "set ac.amount = ac.amount + :chargeAmount where ac.id = :id")
	// void charge(@Param("id") long id, @Param("chargeAmount") long chargeAmount);

}
