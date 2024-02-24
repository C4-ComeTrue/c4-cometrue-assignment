package org.c4marathon.assignment.bankaccount.limit;

import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 충전 한도 관리
 */
@Component
@RequiredArgsConstructor
public class ChargeLimitManager {
	private final RedisTemplate redisTemplate;
	private final MainAccountRepository mainAccountRepository;

	public long get(long pk) {
		return getChargeLimit(pk);
	}

	public boolean charge(long pk, long money) {
		Long limit = getChargeLimit(pk);

		if (limit >= money) {
			limit -= money;
			redisTemplate.opsForValue().set(pk, limit);
			return true;
		}
		return false;
	}

	public Long getChargeLimit(long pk) {
		Long limit = (Long)redisTemplate.opsForValue().get(pk);
		if (limit == null) {
			limit = mainAccountRepository.findChargeLimitByPk(pk)
				.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException("pk에 해당하는 계좌 없음. PK = " + pk));
			redisTemplate.opsForValue().set(pk, limit);
		}

		return limit;
	}
}
