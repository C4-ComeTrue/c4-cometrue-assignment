package org.c4marathon.assignment.global.scheduler;

import java.util.List;

import org.c4marathon.assignment.domain.consumer.service.AfterConsumerService;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.pointlog.repository.PointLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PointScheduler {

	private static final int POINT_EVENT_DELAY = 5 * 60 * 1_000;

	private final PointLogRepository pointLogRepository;
	private final AfterConsumerService afterConsumerService;

	/**
	 * 환불, 또는 구매 확정 이벤트 도중 예외가 발생해도, 5분마다 수행되는 스케줄러에 의해
	 * 수행하지 못한 PointLog의 작업을 수행
	 */
	@Scheduled(fixedDelay = POINT_EVENT_DELAY)
	public void schedulePointEvent() {
		List<PointLog> pointLogs = pointLogRepository.findAll();
		pointLogs.forEach(pointLog -> {
			if (pointLog.getIsConfirm()) {
				afterConsumerService.afterConfirm(pointLog);
			} else {
				afterConsumerService.afterRefund(pointLog);
			}
		});
	}
}
