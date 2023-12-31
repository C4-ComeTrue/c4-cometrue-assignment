package org.c4marathon.assignment.domain.product.repository;

import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Boolean existsByNameAndSeller(String name, Seller seller);
}
