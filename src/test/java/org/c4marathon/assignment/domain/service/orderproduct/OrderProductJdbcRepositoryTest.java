package org.c4marathon.assignment.domain.service.orderproduct;

import static org.mockito.BDDMockito.*;

import java.util.List;

import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductJdbcRepository;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;

public class OrderProductJdbcRepositoryTest extends ServiceTestSupport {

	@InjectMocks
	private OrderProductJdbcRepository orderProductJdbcRepository;
	@Mock
	private JdbcTemplate jdbcTemplate;

	@DisplayName("bulk insert 시")
	@Nested
	class SaveAllBatch {

		@DisplayName("orderProducts에 해당하는 모든 객체를 저장한다.")
		@Test
		void insertAllOf_when_saveAllBatch() {
			orderProductJdbcRepository.saveAllBatch(List.of(orderProduct, orderProduct));
			then(jdbcTemplate)
				.should(times(1))
				.batchUpdate(anyString(), anyList(), anyInt(), any());
		}
	}
}
