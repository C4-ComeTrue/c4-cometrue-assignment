package org.c4marathon.assignment.domain.service.consumer;

import static org.assertj.core.api.Assertions.*;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.consumer.service.ConsumerReadService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsumerReadServiceTest extends ServiceTestSupport {

	@Autowired
	private ConsumerReadService consumerReadService;

	@Autowired
	private ConsumerRepository consumerRepository;

	@DisplayName("이메일로 소비자 조회 시")
	@Nested
	class ExistsByEmail {

		private Consumer consumer;

		@BeforeEach
		void setUp() {
			consumer = consumerRepository.save(Consumer.builder()
				.email("email")
				.address("address")
				.build());
		}

		@AfterEach
		void tearDown() {
			consumerRepository.deleteAllInBatch();
		}

		@DisplayName("email에 해당하는 회원이 존재하면 true를 반환한다.")
		@Test
		void returnTrue_when_existsConsumer() {
			assertThat(consumerReadService.existsByEmail(consumer.getEmail())).isTrue();
		}

		@DisplayName("email에 해당하는 회원이 존재하지 않으면 false를 반환한다.")
		@Test
		void returnFalse_when_notExistsConsumer() {
			assertThat(consumerReadService.existsByEmail(consumer.getEmail() + "1")).isFalse();
		}
	}
}
