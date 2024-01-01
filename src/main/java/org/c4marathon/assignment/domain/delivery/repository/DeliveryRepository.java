package org.c4marathon.assignment.domain.delivery.repository;

import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
