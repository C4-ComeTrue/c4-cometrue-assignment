package org.c4marathon.assignment.domain.service.pay;

import static org.assertj.core.api.Assertions.*;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.domain.pay.dto.request.ChargePayRequest;
import org.c4marathon.assignment.domain.pay.repository.PayRepository;
import org.c4marathon.assignment.domain.pay.service.PayService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PayServiceTest extends ServiceTestSupport {

	@Autowired
	private PayService payService;
	@Autowired
	private PayRepository payRepository;
	@Autowired
	private ConsumerRepository consumerRepository;

	@DisplayName("캐시 충전 시")
	@Nested
	class ChargePay {

		private Consumer consumer;

		@BeforeEach
		void setUp() {
			consumer = consumerRepository.save(Consumer.builder()
				.email("email")
				.address("ad")
				.build());
		}

		@AfterEach
		void tearDown() {
			payRepository.deleteAllInBatch();
			consumerRepository.deleteAllInBatch();
		}

		@DisplayName("amount에 해당하는 캐시가 충전되고, consumer의 balance가 증가한다.")
		@Test
		void addBalance_when_chargePay() {
			ChargePayRequest request = createRequest();

			payService.chargePay(request, consumer);

			assertThat(consumer.getBalance()).isEqualTo(request.getAmount());
		}

		private ChargePayRequest createRequest() {
			return ChargePayRequest.builder()
				.amount(100L)
				.build();
		}
	}
}
