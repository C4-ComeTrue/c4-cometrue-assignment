package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
