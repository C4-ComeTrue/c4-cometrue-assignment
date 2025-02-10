package org.c4marathon.assignment.global.event.withdraw;

import static org.c4marathon.assignment.global.util.Const.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.global.util.StringUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class WithdrawEventListenerTest {

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ListOperations<String, String> listOperations;

	@InjectMocks
	private WithdrawEventListener withdrawEventListener;

	@DisplayName("출금 완료 이벤트가 발생하면 PENDING_DEPOSIT 리스트에 해당 항목이 추가된다.")
	@Test
	void handleWithdrawCompleted() {
		// given
		String transactionId = "tx123";
		Long senderAccountId = 1L;
		Long receiverAccountId = 2L;
		long money = 5000L;

		WithdrawCompletedEvent event = new WithdrawCompletedEvent(transactionId, senderAccountId, receiverAccountId, money);

		given(redisTemplate.opsForList()).willReturn(listOperations);

		// when
		withdrawEventListener.handleWithdrawCompleted(event);

		// then
		verify(listOperations, times(1)).rightPush(
			PENDING_DEPOSIT,
			StringUtil.format("{}:{}:{}:{}",
				transactionId,
				senderAccountId,
				receiverAccountId,
				money
			)
		);
	}
}
