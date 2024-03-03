package org.c4marathon.assignment.bankaccount.scheduler;

import org.c4marathon.assignment.bankaccount.repository.ChargeLimitRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChargeLimitScheduler {
	private final ChargeLimitRepository chargeLimitRepository;

	/**
	 *
	 * 충전 한도를 00시에 초기화 해주는 스케줄러
	 * ChargeLimit 테이블 sapreMoney 값을 CHARGE_LIMIT으로 초기화한다.
	 */
	@Scheduled(cron = "0 0/1 0 * * *")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void chargeLimitSchedule() {
		chargeLimitRepository.bulkSpareMoneyInit();
	}
}
