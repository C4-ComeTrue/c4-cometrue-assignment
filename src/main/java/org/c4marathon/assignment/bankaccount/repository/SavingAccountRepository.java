package org.c4marathon.assignment.bankaccount.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select sa from SavingAccount sa where sa.accountPk = :accountPk")
	Optional<SavingAccount> findByPkForUpdate(@Param("accountPk") long accountPk);

	@Query("select sa from SavingAccount sa where sa.member.memberPk = :memberPk")
	List<SavingAccount> findSavingAccount(long memberPk);
}
