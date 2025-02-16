package org.c4marathon.assignment.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.c4marathon.assignment.entity.SettlementDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SettlementDetailRepository {
	private final JdbcTemplate jdbcTemplate;

	public void saveAll(List<SettlementDetail> settlementDetailList) {
		String sql = """
			INSERT INTO settlement_detail (user_id, settlement_id, amount, status, create_date, updated_date)
			VALUES (?,?,?,?,?,?)
			""";

		jdbcTemplate.batchUpdate(sql,
			settlementDetailList,
			settlementDetailList.size(),
			(PreparedStatement ps, SettlementDetail settlementDetail) -> {
				ps.setLong(1, settlementDetail.getUserId());
				ps.setLong(2, settlementDetail.getSettlement().getId());
				ps.setLong(3, settlementDetail.getAmount());
				ps.setString(4, settlementDetail.getStatus().name());
				ps.setTimestamp(5, Timestamp.from(Instant.now()));
				ps.setTimestamp(6, Timestamp.from(Instant.now()));
			});
	}
}
