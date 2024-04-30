package org.c4marathon.assignment.global.scheduler;

import java.util.List;

import org.c4marathon.assignment.domain.consumer.service.AfterConsumerService;
import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.c4marathon.assignment.domain.pointlog.repository.PointLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PointScheduler {

	private static final int POINT_EVENT_DELAY = 5 * 60 * 1_000;
	private static final int PAGINATION_SIZE = 10;

	private final PointLogRepository pointLogRepository;
	private final AfterConsumerService afterConsumerService;

	/**
	 * 환불, 또는 구매 확정 이벤트 도중 예외가 발생해도, 5분마다 수행되는 스케줄러에 의해
	 * 수행하지 못한 PointLog의 작업을 수행
	 */
	@Scheduled(fixedDelay = POINT_EVENT_DELAY)
	public void schedulePointEvent() {
		Long lastId = 0L;
		while (true) {
			List<PointLog> pointLogs = pointLogRepository.findByIdWithPaging(lastId, PAGINATION_SIZE);
			if (pointLogs.isEmpty()) {
				break;
			} else {
				pointLogs.forEach(pointLog -> {
					if (Boolean.TRUE.equals(pointLog.getIsConfirm())) {
						afterConsumerService.afterConfirm(pointLog);
					} else {
						afterConsumerService.afterRefund(pointLog);
					}
				});
				lastId = pointLogs.get(pointLogs.size() - 1).getId();
			}
		}
	}
}
