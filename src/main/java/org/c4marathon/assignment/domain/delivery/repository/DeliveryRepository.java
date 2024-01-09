package org.c4marathon.assignment.domain.delivery.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

	@Query("select d from Delivery d join fetch d.deliveryCompany where d.id = :id")
	Optional<Delivery> findByIdJoinFetch(@Param("id") Long id);
}
