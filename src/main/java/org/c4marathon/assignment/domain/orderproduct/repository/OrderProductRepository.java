package org.c4marathon.assignment.domain.orderproduct.repository;

import java.util.List;

import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

	@Query("select op from OrderProduct op join fetch op.product where op.order.id = :id")
	List<OrderProduct> findByOrderJoinFetch(@Param("id") Long id);
}
