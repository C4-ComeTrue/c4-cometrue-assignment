package org.c4marathon.assignment.transactional.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.transactional.domain.TransactionalStatus.*;
import static org.c4marathon.assignment.transactional.domain.TransactionalType.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.transactional.domain.TransactionalStatus;
import org.c4marathon.assignment.transactional.domain.TransactionalType;
import org.c4marathon.assignment.transactional.domain.TransferTransactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TransactionalRepositoryTest extends IntegrationTestSupport {

	@Autowired
	private TransactionalRepository transactionalRepository;

	@BeforeEach
	void setUp() {
		List<TransferTransactional> transactionals = List.of(
			TransferTransactional.create(1L, 2L, 1000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now()),
			TransferTransactional.create(2L, 3L, 2000L, IMMEDIATE_TRANSFER, SUCCESS_DEPOSIT, LocalDateTime.now()),
			TransferTransactional.create(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now()),
			TransferTransactional.create(4L, 5L, 4000L, IMMEDIATE_TRANSFER, FAILED_DEPOSIT, LocalDateTime.now()),
			TransferTransactional.create(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now())
		);
		transactionalRepository.saveAll(transactionals);
	}

	@AfterEach
	void tearDown() {
		transactionalRepository.deleteAllInBatch();
	}

	@DisplayName("특정 상태의 트랜잭션을 ID 기반 커서 페이징으로 조회한다.")
	@Test
	void findTransactionalByStatusWithLastId() throws Exception {
	    // given
		TransactionalStatus status = WITHDRAW;
		Long lastId = 1L;
		int pageSize = 2;

		// when
		List<TransferTransactional> result = transactionalRepository.findTransactionalByStatusWithLastId(status,
			lastId, pageSize);

		// then
		assertThat(result).hasSize(2);

		assertThat(result.get(0))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);

		assertThat(result.get(1))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);
	}

	@DisplayName("lastId가 없으면 지정된 pageSize만큼 조회한다.")
	@Test
	void findTransactionalByStatus() throws Exception {
	    // given
		TransactionalStatus status = WITHDRAW;
		int pageSize = 3;

		// when
		List<TransferTransactional> result = transactionalRepository.findTransactionalByStatus(status,
			pageSize);
		// then
		assertThat(result).hasSize(3);

		assertThat(result.get(0))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(1L, 2L, 1000L, IMMEDIATE_TRANSFER, WITHDRAW);

		assertThat(result.get(1))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);

		assertThat(result.get(2))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);
	}

}