package org.c4marathon.assignment.domain.orderproduct.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class OrderProductJdbcRepository {

	private final JdbcTemplate jdbcTemplate;

	@Transactional
	public void saveAllBatch(List<OrderProduct> orderProducts) {
		String sql =
			"insert into order_product_tbl "
				+ "(quantity, order_id, product_id, created_at, updated_at) "
				+ "values (?, ?, ?, ?, ?)";

		jdbcTemplate.batchUpdate(
			sql,
			orderProducts,
			orderProducts.size(),
			(PreparedStatement ps, OrderProduct orderProduct) -> {
				ps.setInt(1, orderProduct.getQuantity());
				ps.setLong(2, orderProduct.getOrder().getId());
				ps.setLong(3, orderProduct.getProduct().getId());
				LocalDateTime now = LocalDateTime.now();
				ps.setTimestamp(4, Timestamp.valueOf(now));
				ps.setTimestamp(5, Timestamp.valueOf(now));
			}
		);
	}
}
