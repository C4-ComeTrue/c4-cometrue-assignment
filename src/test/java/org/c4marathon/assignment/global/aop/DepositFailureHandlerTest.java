package org.c4marathon.assignment.global.aop;

import static org.c4marathon.assignment.global.util.Const.*;
import static org.mockito.Mockito.*;

import org.aspectj.lang.JoinPoint;
import org.c4marathon.assignment.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DepositFailureHandlerTest {

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ListOperations<String, String> listOperations;

	@Mock
	private AccountService accountService;

	@Mock
	private JoinPoint joinPoint;

	@InjectMocks
	private DepositFailureHandler depositFailureHandler;

	@BeforeEach
	void setUp() {
		when(redisTemplate.opsForList()).thenReturn(listOperations);
	}

	@DisplayName("successDeposit에서 예외 발생 시 Redis List PENDING_DEPOSIT을 제거되고, FAILED_DEPOSIT에 추가된다. ")
	@Test
	void handleDepositFailure() throws Exception {

		// given
		String deposit = "tx1:1:2:1000";
		when(joinPoint.getArgs()).thenReturn(new Object[] {deposit});
		// when
		depositFailureHandler.handleDepositFailure(joinPoint, new RuntimeException("예외 발생"));

		// then

		verify(listOperations, times(1)).remove(PENDING_DEPOSIT, 1, deposit);
		verify(listOperations, times(1)).rightPush(FAILED_DEPOSIT, deposit);
	}

	@DisplayName("failedDeposit에서 예외 발생 시 rollbackWithdraw가 호출된다.")
	@Test
	void handleFailedDepositFailure_callsRollbackWithdraw() {
		// given
		String failedDeposit = "tx2:1:2:1000";
		when(joinPoint.getArgs()).thenReturn(new Object[] {failedDeposit});

		// when
		depositFailureHandler.handleFailedDepositFailure(joinPoint, new RuntimeException("예외 발생"));

		// then
		verify(accountService, times(1)).rollbackWithdraw(1L, 1000L);
	}

	@DisplayName("failedDeposit에서 예외 발생 시 FAILED_DEPOSIT 리스트에서 삭제된다.")
	@Test
	void handleFailedDepositFailure_removesFromFailedDepositList() {
		// given
		String failedDeposit = "tx3:1:2:1000";
		when(joinPoint.getArgs()).thenReturn(new Object[] {failedDeposit});

		// when
		depositFailureHandler.handleFailedDepositFailure(joinPoint, new RuntimeException("예외 발생"));

		// then
		verify(listOperations, times(1)).remove(FAILED_DEPOSIT, 1, failedDeposit);
	}
}