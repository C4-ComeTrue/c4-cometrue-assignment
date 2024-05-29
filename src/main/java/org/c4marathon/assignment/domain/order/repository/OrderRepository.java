package org.c4marathon.assignment.domain.order.repository;

import java.util.List;

import org.c4marathon.assignment.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

	boolean existsByConsumerIdAndIdIn(Long consumerId, List<Long> ids);
}
