package org.c4marathon.assignment.global.scheduler;

import static org.mockito.BDDMockito.*;

import java.util.List;

import org.c4marathon.assignment.domain.consumer.service.AfterConsumerService;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class PointSchedulerTest extends ServiceTestSupport {

	@InjectMocks
	private PointScheduler pointScheduler;
	@Mock
	private AfterConsumerService afterConsumerService;

	@DisplayName("스케줄러 실행 시")
	@Nested
	class SchedulePointEvent {

		@DisplayName("pointLog의 isConfirm에 따라 service가 실행된다.")
		@Test
		void should_invokeAfterConsumerService_when_schedulerIsActive() {

			PointLog confirmLog = mock(PointLog.class);
			given(confirmLog.getIsConfirm()).willReturn(true);
			PointLog refundLog = mock(PointLog.class);
			given(refundLog.getIsConfirm()).willReturn(false);

			willDoNothing()
				.given(afterConsumerService)
				.afterConfirm(any(PointLog.class));
			willDoNothing()
				.given(afterConsumerService)
				.afterRefund(any(PointLog.class));

			given(pointLogRepository.findAll())
				.willReturn(List.of(confirmLog, refundLog));

			pointScheduler.schedulePointEvent();

			then(afterConsumerService)
				.should(times(1))
				.afterRefund(any(PointLog.class));
			then(afterConsumerService)
				.should(times(1))
				.afterConfirm(any(PointLog.class));
		}
	}
}
