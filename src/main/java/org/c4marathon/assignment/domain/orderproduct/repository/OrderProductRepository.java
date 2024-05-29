package org.c4marathon.assignment.domain.orderproduct.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

	@Query("select op from OrderProduct op join fetch op.product where op.order.id = :id")
	List<OrderProduct> findByOrderJoinFetchProduct(@Param("id") Long id);

	@Query("select op from OrderProduct op join fetch op.product join fetch op.product.seller where op.order.id = :id")
	List<OrderProduct> findByOrderJoinFetchProductAndSeller(@Param("id") Long orderId);

	@Query(value = "select order_id from order_product_tbl op where op.product_id = :product_id", nativeQuery = true)
	List<Long> findOrderIdByProductId(@Param("product_id") Long id);

	@Modifying
	@Query(value = """
		delete
		from order_product_tbl op
		where op.created_at <= :dateTime
		limit :pageSize
		""", nativeQuery = true)
	int deleteOrderProductTable(@Param("dateTime") LocalDateTime dateTime, @Param("pageSize") int pageSize);
}
