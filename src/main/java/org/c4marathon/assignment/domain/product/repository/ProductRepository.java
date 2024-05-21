package org.c4marathon.assignment.domain.product.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Boolean existsByNameAndSeller(String name, Seller seller);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Product p join fetch p.seller where p.id = :id")
	Optional<Product> findByIdJoinFetch(@Param("id") Long id);

	@Query(value = """
		select count(*)
		from product_tbl p
		where p.product_id = :product_id
		""", nativeQuery = true)
	Long findReviewCount(@Param("product_id") Long id);

	@Query(value = """
		select *
		from product_tbl p
		where name like :keyword
		  and (p.created_at < :createdAt or (p.created_at = :createdAt and p.product_id > :id))
		order by p.created_at desc, p.product_id
		limit :pageSize
		""", nativeQuery = true)
	List<Product> findByNewest(
		@Param("keyword") String keyword,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("id") Long id,
		@Param("pageSize") int pageSize
	);

	@Query(value = """
		select *
		from product_tbl p
		where name like :keyword
		  and (p.amount > :amount or (p.amount = :amount and p.product_id > :id))
		order by p.amount, p.product_id
		limit :pageSize
		""", nativeQuery = true)
	List<Product> findByPriceAsc(
		@Param("keyword") String keyword,
		@Param("amount") Long amount,
		@Param("id") Long id,
		@Param("pageSize") int pageSize
	);

	@Query(value = """
		select *
		from product_tbl p
		where (p.amount < :amount or (p.amount = :amount and p.product_id > :id))
		  and name like :keyword
		order by p.amount desc, p.product_id
		limit :pageSize
		""", nativeQuery = true)
	List<Product> findByPriceDesc(
		@Param("keyword") String keyword,
		@Param("amount") Long amount,
		@Param("id") Long id,
		@Param("pageSize") int pageSize
	);

	@Query(value = """
		select *
		from product_tbl
		where name like :keyword
		    and order_count <= :order_count
		   or (order_count = :order_count and product_id > :product_id)
		order by order_count desc, product_id
		limit :pageSize
		""", nativeQuery = true)
	List<Product> findByPopularity(
		@Param("keyword") String keyword,
		@Param("order_count") Long orderCount,
		@Param("product_id") Long id,
		@Param("pageSize") int pageSize
	);

	@Query(value = """
		select *
		from product_tbl
		where name like :keyword
		    and avg_score <= :avgScore
		   or (avg_score = :avgScore and product_id > :product_id)
		order by avg_score desc, product_id
		limit :pageSize
		""", nativeQuery = true)
	List<Product> findByTopRated(
		@Param("keyword") String keyword,
		@Param("avgScore") Double avgScore,
		@Param("product_id") Long id,
		@Param("pageSize") int pageSize
	);
}
