package org.c4marathon.assignment.domain.orderproduct.repository;

import java.util.List;

import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

	@Query("select op from OrderProduct op join fetch op.product where op.order.id = :id")
	List<OrderProduct> findByOrderJoinFetchProduct(@Param("id") Long id);

	@Query("select op from OrderProduct op join fetch op.product join fetch op.product.seller where op.order.id = :id")
	List<OrderProduct> findByOrderJoinFetchProductAndSeller(@Param("id") Long orderId);
}
