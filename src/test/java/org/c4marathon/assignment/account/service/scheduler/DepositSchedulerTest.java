package org.c4marathon.assignment.account.service.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.util.Const.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.c4marathon.assignment.account.service.DepositService;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.c4marathon.assignment.global.util.StringUtil;
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
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DepositSchedulerTest {

	@Mock
	private DepositService depositService;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ListOperations<String, String> listOperations;

	@InjectMocks
	private DepositScheduler depositScheduler;

	private MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);

	@BeforeEach
	void setUp() {
		when(redisTemplate.opsForList()).thenReturn(listOperations);
		ReflectionTestUtils.setField(depositScheduler, "threadPoolExecutor", threadPoolExecutor);
	}

	@DisplayName("ThreadPool이 초기화되고 모든 태스크가 병렬로 처리되는지 검증")
	@Test
	void depositsParallelExecution() throws Exception {
	    // given
		int numberOfDeposits = 16;
		CountDownLatch processLatch = new CountDownLatch(numberOfDeposits);
		AtomicInteger concurrentExecutions = new AtomicInteger(0);
		AtomicInteger maxConcurrentExecutions = new AtomicInteger(0);

		List<String> deposits = new ArrayList<>();
		for (int i = 0; i < numberOfDeposits; i++) {
			deposits.add(StringUtil.format("tx{}:{}:{}:{}", i, i + 1L, i + 2L, 1000));
		}
		given(listOperations.range(PENDING_DEPOSIT, 0, -1)).willReturn(deposits);

		doAnswer(invocation -> {
			int current = concurrentExecutions.incrementAndGet();
			maxConcurrentExecutions.updateAndGet(max -> Math.max(max, current));
			Thread.sleep(100);
			concurrentExecutions.decrementAndGet();
			processLatch.countDown();
			return null;
		}).when(depositService).successDeposit(anyString());

	    // when
		long startTime = System.currentTimeMillis();
		depositScheduler.deposits();
		boolean allProcessed = processLatch.await(5, TimeUnit.SECONDS);

		// then
		assertThat(allProcessed).isTrue();
		assertThat(maxConcurrentExecutions.get()).isEqualTo(8);
		long executionTime = System.currentTimeMillis() - startTime;
		assertThat(executionTime).isLessThan(1000L);
	}

	@DisplayName("같은 계좌에 동시에 입금 요청이 와도 동시성 문제 없이 정확한 금액이 입금된다.")
	@Test
	void depositsConcurrency() throws Exception {
	    // given
		int numberOfDeposits = 100;
		CountDownLatch processLatch = new CountDownLatch(numberOfDeposits);
		long expectedTotalAmount = IntStream.range(0, numberOfDeposits)
			.mapToLong(i -> 100L * i)
			.sum();

		AtomicLong actualTotalAmount = new AtomicLong(0);

		List<String> deposits = IntStream.range(0, numberOfDeposits)
			.mapToObj(i -> StringUtil.format("tx{}:{}:{}:{}", i, i + 3L, 2L, 100 * i))
			.toList();
		given(listOperations.range(PENDING_DEPOSIT, 0, -1)).willReturn(deposits);

		doAnswer(invocation -> {
			String deposit = invocation.getArgument(0);
			long amount = Long.parseLong(deposit.split(":")[3]);
			actualTotalAmount.addAndGet(amount);
			Thread.sleep(10);
			processLatch.countDown();
			return null;
		}).when(depositService).successDeposit(anyString());

		// when
		depositScheduler.deposits();
		boolean allProcessed = processLatch.await(5, TimeUnit.SECONDS);

		// then
		assertThat(allProcessed).isTrue();
		assertThat(actualTotalAmount.get()).isEqualTo(expectedTotalAmount);
		verify(depositService, times(numberOfDeposits)).successDeposit(anyString());
	}

	@DisplayName("PENDING_DEPOSIT에서 데이터를 가져와 successDeposit이 호출된다.")
	@Test
	void deposits() {
	    // given
		List<String> pendingDeposits = List.of(
			"tx1:1:2:1000",
			"tx2:3:4:2000"
		);
		when(listOperations.range(PENDING_DEPOSIT, 0, -1)).thenReturn(pendingDeposits);

		// when
		depositScheduler.deposits();

	    // then
		verify(depositService, times(1)).successDeposit("tx1:1:2:1000");
		verify(depositService, times(1)).successDeposit("tx2:3:4:2000");
	}

	@DisplayName("PENDING_DEPOSIT에 데이터가 없으면 successDeposit이 호출되지 않는다.")
	@Test
	void deposits_emptyList() {
		// given
		when(listOperations.range(PENDING_DEPOSIT, 0, -1)).thenReturn(List.of());

		// when
		depositScheduler.deposits();

		// then
		verify(depositService, never()).successDeposit(any());
	}

	@DisplayName("FAILED_DEPOSIT에서 데이터를 가져와 failedDeposit이 호출된다.")
	@Test
	void rollbackDeposits() {
		// given
		List<String> failedDeposits = List.of("tx3:5:6:500", "tx4:7:8:1500");
		when(listOperations.range(FAILED_DEPOSIT, 0, -1)).thenReturn(failedDeposits);

		// when
		depositScheduler.rollbackDeposits();

		// then
		verify(depositService, times(1)).failedDeposit("tx3:5:6:500");
		verify(depositService, times(1)).failedDeposit("tx4:7:8:1500");
	}

	@DisplayName("FAILED_DEPOSIT에 데이터가 없으면 failedDeposit이 호출되지 않는다.")
	@Test
	void rollbackDeposits_emptyList() {
		// given
		when(listOperations.range(FAILED_DEPOSIT, 0, -1)).thenReturn(List.of());

		// when
		depositScheduler.rollbackDeposits();

		// then
		verify(depositService, never()).failedDeposit(any());
	}
}
