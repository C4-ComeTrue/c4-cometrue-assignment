package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ac from Account ac where ac.id = :id")
	Optional<Account> findByIdWithWriteLock(@Param("id") long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ac from Account ac where ac.member.id = :id")
	Optional<Account> findByMemberIdWithWriteLock(@Param("id") long memberId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ac from Account ac where ac.accountNumber = :accountNumber")
	Optional<Account> findByAccountNumberWithWriteLock(String accountNumber);

	// Optional<Account> findById(long id);
	//
	// Optional<Account> findByMemberId(long memberId);
	//
	// update + select 를 한 구문에 둔다면? -> 원자성 처럼 연산할 수 있나..
	// @Modifying
	// @Query("update Account ac "
	// 	+ "set ac.amount = ac.amount + :chargeAmount, "
	// 	+ "ac.accumulatedChargeAmount = ac.accumulatedChargeAmount + :chargeAmount "
	// 	+ "where ac.id = :id")
	// void charge(@Param("id") long id, @Param("chargeAmount") long chargeAmount);
	//
	// @Query("select ac.amount from Account ac where ac.id = :id")
	// long findAmount(@Param("id") long id);
    //
	// // 계좌에서 잔액을 빼내는 작업
	// @Modifying
	// @Query("update Account ac "
	// 	+ "set ac.amount = ac.amount - :amount "
	// 	+ "where ac.id = :id and ac.amount >= :amount")
	// void withdraw(@Param("id") long id, @Param("amount") long amount);
}
