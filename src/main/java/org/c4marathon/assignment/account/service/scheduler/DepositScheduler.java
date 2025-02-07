package org.c4marathon.assignment.account.service.scheduler;

import static org.c4marathon.assignment.global.util.Const.*;

import java.util.List;

import org.c4marathon.assignment.account.service.DepositService;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositScheduler {
	private final DepositService depositService;
	private final RedisTemplate<String, String> redisTemplate;
	private final MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);

	@Scheduled(fixedRate = 10000)
	public void deposits() {
		threadPoolExecutor.init();

		List<String> deposits = redisTemplate.opsForList().range(PENDING_DEPOSIT, 0, -1);
		if (deposits == null || deposits.isEmpty()) {
			return;
		}

		for (String deposit : deposits) {
			threadPoolExecutor.execute(() -> depositService.successDeposit(deposit));
		}

		try {
			threadPoolExecutor.waitToEnd();
		} catch (Exception e) {
			log.error("스레드 풀 실행 중 예외 발생 : {}", e.getMessage(), e);
		}
	}

	/**
	 * 입금 실패한 경우가 많이 없을 것이라고 생각하여 멀티 스레드 X
	 *  나중에 멀티 스레드 성능 테스트 후 결정
	 */
	@Scheduled(fixedRate = 12000)
	public void rollbackDeposits() {
		List<String> failedDeposits = redisTemplate.opsForList().range(FAILED_DEPOSIT, 0, -1);
		if (failedDeposits == null || failedDeposits.isEmpty()) {
			return;
		}

		for (String depositRequest : failedDeposits) {
			depositService.failedDeposit(depositRequest);
		}
	}
}
