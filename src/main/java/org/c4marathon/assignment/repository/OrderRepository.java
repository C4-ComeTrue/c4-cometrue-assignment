package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.Order;
import org.c4marathon.assignment.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	Order findOrderBySellerAndOrderStatus(Member seller, OrderStatus orderStatus);
}
