package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findById(long id);

	Optional<Account> findByMemberId(long memberId);

	Optional<Account> findByAccountNumber(String accountNumber);

	@Query("select ac.amount from Account ac where ac.id = :id")
	long findAmount(@Param("id") long id);

	@Modifying
	@Query("update Account ac "
		+ "set ac.amount = ac.amount + :chargeAmount, "
		+ "ac.accumulatedChargeAmount = ac.accumulatedChargeAmount + :chargeAmount "
		+ "where ac.id = :id")
	void charge(@Param("id") long id, @Param("chargeAmount") long chargeAmount);

	@Modifying
	@Query("update Account ac "
		+ "set ac.amount = ac.amount + :amount where ac.id = :id")
	void deposit(@Param("id") long id, @Param("amount") long amount);

	@Modifying
	@Query("update Account ac "
		+ "set ac.amount = ac.amount - :amount "
		+ "where ac.id = :id and ac.amount >= :amount")
	int withdraw(@Param("id") long id, @Param("amount") long amount);
}
