package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

}
