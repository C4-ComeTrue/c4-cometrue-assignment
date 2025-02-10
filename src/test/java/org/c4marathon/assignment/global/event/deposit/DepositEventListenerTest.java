package org.c4marathon.assignment.global.event.deposit;

import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.global.util.Const;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ListOperations;

@ExtendWith(MockitoExtension.class)
class DepositEventListenerTest {

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ListOperations<String, String> listOperations;

	@InjectMocks
	private DepositEventListener depositEventListener;

	@DisplayName("입금 완료 이벤트가 발생하면 PENDING_DEPOSIT 리스트에서 해당 항목이 삭제된다.")
	@Test
	void handleDepositCompleted() {
		// given
		String depositInfo = "tx1:1:2:5000";
		DepositCompletedEvent event = new DepositCompletedEvent(depositInfo);

		given(redisTemplate.opsForList()).willReturn(listOperations);

		// when
		depositEventListener.handleDepositCompleted(event);

		// then
		verify(listOperations, times(1)).remove(Const.PENDING_DEPOSIT, 1, depositInfo);
	}
}
