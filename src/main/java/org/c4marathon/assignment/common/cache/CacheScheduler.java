package org.c4marathon.assignment.common.cache;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheScheduler {
	private final RedisTemplate<String, Long> redisTemplate;
	private final MainAccountRepository mainAccountRepository;
	static final long chargeLimit = 3000000L;

	@Scheduled(cron = "00 50 23 * * *")
	public void resetDailyLimit() {

		/* 23시 50분 부터 20분동안 은행 계좌 점검 => 20분동안 일일 한도 초기화 */
		String lockKey = "reset";
		redisTemplate.opsForValue().set(lockKey, 1L, Duration.ofMinutes(20));

		/* DB의 chargeLimit값 복구 후 모든 Redis의 dailyLimit 삭제 */
		try {
			Set<String> keys = redisTemplate.keys("dailyLimit:*");
			if (keys != null) {
				for (String key : keys) {
					String accountId = key.split(":")[1];
					mainAccountRepository.updateChargeLimit(Long.parseLong(accountId), chargeLimit);
					redisTemplate.delete(key);
				}
			}
		} catch (Exception e) {
			log.error("일일 충전 한도 초기화 중 오류 발생", e);
		} finally {
			redisTemplate.delete(lockKey);
		}
	}
}
