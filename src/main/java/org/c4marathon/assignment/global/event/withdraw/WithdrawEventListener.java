package org.c4marathon.assignment.global.event.withdraw;

import static org.c4marathon.assignment.global.util.Const.*;

import org.c4marathon.assignment.global.util.StringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WithdrawEventListener {

	private final RedisTemplate<String, String> redisTemplate;

	@TransactionalEventListener
	public void handleWithdrawCompleted(WithdrawCompletedEvent event) {
		redisTemplate.opsForList().rightPush(
			PENDING_DEPOSIT,
			StringUtil.format("{}:{}:{}:{}",
				event.transactionId(),
				event.senderAccountId(),
				event.receiverAccountId(),
				event.money()
			)
		);
	}
}
