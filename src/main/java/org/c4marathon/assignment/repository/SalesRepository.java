package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.OrderItem;
import org.c4marathon.assignment.domain.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalesRepository extends JpaRepository<Sales, Long> {

	@Query("select s from Sales s where s.orderItem = :orderItem")
	List<Sales> findAllByOrderItem(OrderItem orderItem);

	@Query("select s from Sales s where s.receiver = :seller or s.sender = :seller")
	List<Sales> findAllBySeller(Member seller);

	@Query("select s from Sales s join fetch OrderItem o where o.item = :item")
	List<Sales> findAllByItem(Item item);

}
