package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

	Optional<Shipment> findByTrackingNumber(String trackingNumber);

	Optional<Shipment> findByOrder(Order order);

}
