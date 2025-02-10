package org.c4marathon.assignment.global.event.deposit;

import static org.c4marathon.assignment.global.util.Const.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DepositEventListener {
	private final RedisTemplate<String, String> redisTemplate;

	@TransactionalEventListener
	public void handleDepositCompleted(DepositCompletedEvent event) {
		redisTemplate.opsForList().remove(PENDING_DEPOSIT, 1, event.deposit());
	}
}
