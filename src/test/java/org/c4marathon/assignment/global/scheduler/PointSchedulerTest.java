package org.c4marathon.assignment.global.scheduler;

import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;

import org.c4marathon.assignment.domain.consumer.service.ConsumerReadService;
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
	private ConsumerReadService consumerReadService;

	@DisplayName("스케줄러 실행 시")
	@Nested
	class SchedulePointEvent {

		@DisplayName("pointLog의 isConfirm이 true이면 afterConfirm이 실행된다.")
		@Test
		void should_invokeAfterConfirm_when_isConfirm() {

			PointLog confirmLog = mock(PointLog.class);
			given(confirmLog.getIsConfirm()).willReturn(true);

			given(pointLogRepository.findByIdWithPaging(anyLong(), anyInt()))
				.willReturn(List.of(confirmLog))
				.willReturn(Collections.emptyList());
			given(consumerReadService.findById(anyLong()))
				.willReturn(consumer);

			pointScheduler.schedulePointEvent();

			then(consumer)
				.should(times(0))
				.addBalance(anyLong());
			then(pointLogRepository)
				.should(times(1))
				.deleteById(anyLong());
		}

		@DisplayName("pointLog의 isConfirm이 false이면 afterRefund가 실행된다.")
		@Test
		void should_invokeAfterRefund_when_isNotConfirm() {

			PointLog refundLog = mock(PointLog.class);
			given(refundLog.getIsConfirm()).willReturn(false);

			given(pointLogRepository.findByIdWithPaging(anyLong(), anyInt()))
				.willReturn(List.of(refundLog))
				.willReturn(Collections.emptyList());
			given(consumerReadService.findById(anyLong()))
				.willReturn(consumer);

			pointScheduler.schedulePointEvent();

			then(consumer)
				.should(times(1))
				.addBalance(anyLong());
			then(pointLogRepository)
				.should(times(1))
				.deleteById(anyLong());
		}

		@DisplayName("pointLog가 조회되지 않으면 스케줄러가 종료된다.")
		@Test
		void should_terminateSchedule_when_notExistPointLog() {
			given(pointLogRepository.findByIdWithPaging(anyLong(), anyInt()))
				.willReturn(Collections.emptyList());

			pointScheduler.schedulePointEvent();

			then(pointLogRepository)
				.should(times(0))
				.deleteById(anyLong());
		}
	}
}
