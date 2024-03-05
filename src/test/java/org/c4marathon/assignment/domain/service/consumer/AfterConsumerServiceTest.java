package org.c4marathon.assignment.domain.service.consumer;

import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.service.AfterConsumerService;
import org.c4marathon.assignment.domain.consumer.service.ConsumerReadService;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class AfterConsumerServiceTest extends ServiceTestSupport {

	@InjectMocks
	private AfterConsumerService afterConsumerService;
	@Mock
	private ConsumerReadService consumerReadService;

	@DisplayName("거래 확정 이후")
	@Nested
	class AfterConfirm {

		@DisplayName("PointLog에 저장된 회원의 포인트는 earnedPoint만큼 증가한다.")
		@Test
		void should_updatePoint_when_eventOccurred() {
			PointLog pointLog = new PointLog(1L, 100L, 100L, 100L, true);

			Consumer consumer = mock(Consumer.class);
			given(consumerReadService.findById(anyLong()))
				.willReturn(consumer);

			afterConsumerService.afterConfirm(pointLog);

			then(consumer)
				.should(times(1))
				.updatePoint(anyLong());
		}

		@DisplayName("PointLog는 삭제되어야 한다.")
		@Test
		void should_deletePointLog_when_eventOccurred() {
			PointLog pointLog = new PointLog(1L, 100L, 100L, 100L, true);

			Consumer consumer = mock(Consumer.class);
			given(consumerReadService.findById(anyLong()))
				.willReturn(consumer);

			afterConsumerService.afterConfirm(pointLog);

			then(pointLogRepository)
				.should(times(1))
				.deleteById(any());
		}
	}

	@DisplayName("거래 환불 이후")
	@Nested
	class AfterRefund {

		@DisplayName("회원의 포인트는 usedPoint만큼 증가하고, balance는 totalAmount - usedPoint 만큼 증가한다.")
		@Test
		void should_updatePointEndBalance_when_eventOccurred() {
			PointLog pointLog = new PointLog(1L, 100L, 100L, 100L, true);

			Consumer consumer = mock(Consumer.class);
			given(consumerReadService.findById(anyLong()))
				.willReturn(consumer);

			afterConsumerService.afterRefund(pointLog);

			then(consumer)
				.should(times(1))
				.addBalance(anyLong());
			then(consumer)
				.should(times(1))
				.updatePoint(anyLong());
		}

		@DisplayName("PointLog는 삭제되어야 한다.")
		@Test
		void should_deletePointLog_when_eventOccurred() {
			PointLog pointLog = new PointLog(1L, 100L, 100L, 100L, true);

			Consumer consumer = mock(Consumer.class);
			given(consumerReadService.findById(anyLong()))
				.willReturn(consumer);

			afterConsumerService.afterRefund(pointLog);

			then(pointLogRepository)
				.should(times(1))
				.deleteById(any());
		}
	}
}
