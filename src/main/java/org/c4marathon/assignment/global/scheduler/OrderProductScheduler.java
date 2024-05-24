package org.c4marathon.assignment.global.scheduler;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.orderproduct.repository.OrderProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderProductScheduler {

	private static final int PAGINATION_SIZE = 1000;
	private static final int EXISTS_DAY = 30;

	private final OrderProductRepository orderProductRepository;

	/**
	 * 24시간 마다 수행되는 스케줄러로, 30일 이전의 주문 내역을 삭제
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	@Transactional
	public void scheduleDeleteOrderProducts() {
		int deletedCount;
		do {
			LocalDateTime dateTime = LocalDateTime.now().minusDays(EXISTS_DAY);
			deletedCount = orderProductRepository.deleteOrderProductTable(dateTime, PAGINATION_SIZE);
		} while (deletedCount != 0);
	}
}
