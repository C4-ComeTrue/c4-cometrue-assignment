package org.c4marathon.assignment.bankaccount.repository;

import java.util.Optional;

import org.c4marathon.assignment.bankaccount.entity.SavingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SavingProductRepository extends JpaRepository<SavingProduct, Long> {
	@Query("select sp.productRate from SavingProduct sp where sp.productName = :productName")
	Optional<Integer> findRateByProductName(String productName);
}
