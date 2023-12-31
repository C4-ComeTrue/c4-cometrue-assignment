package org.c4marathon.assignment.domain.seller.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {

	Boolean existsByEmail(String email);

	Optional<Seller> findByEmail(String email);
}
