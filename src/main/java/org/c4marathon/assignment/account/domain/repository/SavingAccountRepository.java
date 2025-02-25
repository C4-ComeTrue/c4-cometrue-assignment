package org.c4marathon.assignment.account.domain.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.SavingProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {

	Optional<SavingAccount> findBySavingAccountNumber(String savingAccountNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT sa
		FROM SavingAccount sa
		WHERE sa.savingAccountNumber = :savingAccountNumber
		""")
	Optional<SavingAccount> findBySavingAccountNumberWithLock(@Param("savingAccountNumber") String savingAccountNumber);

	@Query("""
		SELECT sa
		FROM SavingAccount sa
		JOIN FETCH sa.savingProduct sp
		WHERE sp.type = :type
		ORDER BY sa.id
		LIMIT :size
		""")
	List<SavingAccount> findSavingAccountByFixed(
		@Param("type") SavingProductType type,
		@Param("size") int size
	);

	//index(lastId, type)
	@Query("""
		SELECT sa
		FROM SavingAccount sa
		JOIN FETCH sa.savingProduct sp
		WHERE sp.type = :type AND sa.id > :lastId
		ORDER BY sa.id
		LIMIT :size
		""")
	List<SavingAccount> findSavingAccountByFixedWithLastId(
		@Param("type") SavingProductType type,
		@Param("lastId") Long lastId,
		@Param("size") int size
	);

	@Query("""
		SELECT sa
		FROM SavingAccount sa
		JOIN FETCH sa.savingProduct sp
		ORDER BY sa.id
		LIMIT :size
		""")
	List<SavingAccount> findAllSavingAccount(@Param("size") int size);

	@Query("""
		SELECT sa
		FROM SavingAccount sa
		JOIN FETCH sa.savingProduct sp
		WHERE sa.id > :lastId
		ORDER BY sa.id
		LIMIT :size
		""")
	List<SavingAccount> findAllSavingAccountByLastId(
		@Param("lastId") Long lastId,
		@Param("size") int size
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT sa
		FROM SavingAccount sa
		WHERE sa.savingAccountNumber = :freeSavingAccountNumber
		AND sa.savingProduct.type = :savingProductType
		""")
	Optional<SavingAccount> findFreeSavingAccountWithLock(
		@Param("freeSavingAccountNumber") String freeSavingAccountNumber,
		@Param("savingProductType") SavingProductType savingProductType
	);
}
