package org.c4marathon.assignment.domain.order.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("select o from Order o join fetch o.consumer join fetch o.delivery where o.id = :id")
	Optional<Order> findByIdJoinFetch(@Param("id") Long id);
}
