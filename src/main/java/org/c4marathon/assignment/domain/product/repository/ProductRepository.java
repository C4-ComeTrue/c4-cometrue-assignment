package org.c4marathon.assignment.domain.product.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Boolean existsByNameAndSeller(String name, Seller seller);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Product p join fetch p.seller where p.id = :id")
	Optional<Product> findByIdJoinFetch(@Param("id") Long id);
}
