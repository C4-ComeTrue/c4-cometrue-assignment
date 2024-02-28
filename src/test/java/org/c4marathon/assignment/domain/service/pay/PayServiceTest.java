package org.c4marathon.assignment.domain.service.pay;

import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.pay.dto.request.ChargePayRequest;
import org.c4marathon.assignment.domain.pay.entity.Pay;
import org.c4marathon.assignment.domain.pay.service.PayService;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

public class PayServiceTest extends ServiceTestSupport {

	@InjectMocks
	private PayService payService;

	@DisplayName("캐시 충전 시")
	@Nested
	class ChargePay {

		@DisplayName("amount에 해당하는 캐시가 충전되고, consumer의 balance가 증가한다.")
		@Test
		void addBalance_when_chargePay() {
			ChargePayRequest request = createRequest();

			payService.chargePay(request, consumer);

			then(payRepository)
				.should(times(1))
				.save(any(Pay.class));
			then(consumer)
				.should(times(1))
				.addBalance(anyLong());
			then(consumerRepository)
				.should(times(1))
				.save(any(Consumer.class));
		}

		private ChargePayRequest createRequest() {
			return new ChargePayRequest(100L);
		}
	}
}
