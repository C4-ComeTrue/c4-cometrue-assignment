package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.c4marathon.assignment.global.util.StringUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.c4marathon.assignment.global.util.Const.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ListOperations<String, String> listOperations;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Spy
	private MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);
	private DepositService depositService;

	@BeforeEach
	void setUp() {
		depositService = new DepositService(accountRepository, redisTemplate);
		ReflectionTestUtils.setField(depositService, "threadPoolExecutor", threadPoolExecutor);
		when(redisTemplate.opsForList()).thenReturn(listOperations);
	}

	@DisplayName("ThreadPool이 초기화되고 모든 태스크가 병렬로 처리되는지 검증")
	@Test
	void depositsWithMultiThread() throws Exception {
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

		for (int i = 0; i < numberOfDeposits; i++) {
			Account account = mock(Account.class);
			given(accountRepository.findByIdWithLock(i + 2L)).willReturn(Optional.of(account));

			doAnswer(invocation -> {
				int current = concurrentExecutions.incrementAndGet();
				maxConcurrentExecutions.updateAndGet(max -> Math.max(max, current));
				Thread.sleep(100);
				concurrentExecutions.decrementAndGet();
				processLatch.countDown();
				return null;
			}).when(account).deposit(anyLong());
		}

		// When
		long startTime = System.currentTimeMillis();
		depositService.deposits();
		boolean allProcessed = processLatch.await(5, TimeUnit.SECONDS);

		// Then
		assertThat(allProcessed).isTrue();
		assertThat(maxConcurrentExecutions.get()).isEqualTo(8); // threadCount
		long executionTime = System.currentTimeMillis() - startTime;
		// 순차 처리였다면 16 * 100ms = 1600ms 걸렸을 것
		assertThat(executionTime).isLessThan(1000L);
	}

	@DisplayName("같은 계좌에 동시에 입금 요청이 와도 동시성 문제 없이 정확한 금액이 입금된다.")
	@Test
	void deposits_ConcurrencyTest() throws Exception {

		// given
		int numberOfDeposits = 100;
		CountDownLatch processLatch = new CountDownLatch(numberOfDeposits);

		long expectedTotalAmount = IntStream.range(0, numberOfDeposits)
			.mapToLong(i -> 100L * i)
			.sum();

		AtomicLong actualTotalAmount = new AtomicLong(0);

		ConcurrentHashMap<Long, AtomicInteger> accountDepositCounts = new ConcurrentHashMap<>();
		ConcurrentHashMap<Long, AtomicLong> accountDepositAmounts = new ConcurrentHashMap<>();

		// 같은 계좌에 대한 여러 입금 요청 생성
		List<String> deposits = IntStream.range(0, numberOfDeposits)
			.mapToObj(i -> StringUtil.format("tx{}:{}:{}:{}", i, i + 3L, 2L, 100 * i))
			.toList();

		given(listOperations.range(PENDING_DEPOSIT, 0, -1))
			.willReturn(deposits);

		Account account = mock(Account.class);
		given(accountRepository.findByIdWithLock(2L))
			.willReturn(Optional.of(account));

		// 동시성 및 정확성 검증을 위한 모킹
		doAnswer(invocation -> {
			long amount = invocation.getArgument(0);
			accountDepositCounts.computeIfAbsent(2L, k -> new AtomicInteger(0)).incrementAndGet(); // 동시 실행 계수 추적
			actualTotalAmount.addAndGet(amount); // 실제 입금 총액 추적
			accountDepositAmounts.computeIfAbsent(2L, k -> new AtomicLong(0)).addAndGet(amount); // 계좌별 입금 금액 추적
			Thread.sleep(10); // 입금 시뮬레이션을 위한 작은 지연
			processLatch.countDown();
			return null;
		}).when(account).deposit(anyLong());

		// when
		depositService.deposits();
		boolean allProcessed = processLatch.await(5, TimeUnit.SECONDS);

		// then
		assertThat(allProcessed).isTrue(); // 모든 입금 요청이 처리되었는지 검증
		assertThat(accountDepositCounts.get(2L).get()).isEqualTo(numberOfDeposits); // 입금 횟수 검증
		assertThat(actualTotalAmount.get()).isEqualTo(expectedTotalAmount); // 총 입금 금액 검증
	}

	@DisplayName("Redis에 입금 대기 데이터가 존재할 경우, 모든 입금 요청을 처리하고 Redis에서 삭제한다.")
	@Test
	void deposit_Success() {
		// given
		List<String> deposits = List.of(
			"tx1:1:2:1000",
			"tx2:2:3:3000",
			"tx3:3:4:4000"
		);
		given(listOperations.range(PENDING_DEPOSIT, 0, -1)).willReturn(deposits);

		Account account1 = mock(Account.class);
		Account account2 = mock(Account.class);
		Account account3 = mock(Account.class);

		given(accountRepository.findByIdWithLock(2L)).willReturn(Optional.of(account1));
		given(accountRepository.findByIdWithLock(3L)).willReturn(Optional.of(account2));
		given(accountRepository.findByIdWithLock(4L)).willReturn(Optional.of(account3));

		// when
		depositService.deposits();

		// then
		verify(listOperations, times(3)).remove(eq(PENDING_DEPOSIT), anyLong(), anyString());
		verify(accountRepository, times(3)).save(any(Account.class));

		verify(account1).deposit(1000);
		verify(account2).deposit(3000);
		verify(account3).deposit(4000);
	}

	@DisplayName("Redis에 입금 대기 데이터가 없는 경우, 입금 처리를 하지 않는다.")
	@Test
	void deposits_ShouldNotProcess_WhenNoPendingDepositsExist() throws Exception {

		// given
		given(listOperations.range(PENDING_DEPOSIT, 0, -1)).willReturn(List.of());

		// when
		depositService.deposits();

		// then
		verify(listOperations, times(1)).range(PENDING_DEPOSIT, 0, -1);
		verify(listOperations, never()).remove(eq(PENDING_DEPOSIT), anyLong(), anyString());
	}

	@DisplayName("입금 실패 시 failed-deposits로 데이터가 저장된다.")
	@Test
	void deposits_Failure() {
		// given
		String deposit = "tx1:1:2:1000";
		given(listOperations.range(PENDING_DEPOSIT, 0, -1)).willReturn(List.of(deposit));
		given(accountRepository.findByIdWithLock(2L)).willThrow(new NotFoundAccountException());

		// when
		depositService.deposits();

		// then
		verify(listOperations).remove(eq(PENDING_DEPOSIT), eq(1L), eq(deposit));
		verify(listOperations).rightPush(FAILED_DEPOSIT, deposit);
	}

	@DisplayName("실패한 입금들은 재시도를 한다.")
	@Test
	void rollbackDeposits_RetrySuccess() throws Exception {
		// given
		String failedDeposit = "tx1:1:2:1000";
		given(listOperations.range(FAILED_DEPOSIT, 0, -1))
			.willReturn(List.of(failedDeposit));

		Account receiverAccount = mock(Account.class);
		given(accountRepository.findByIdWithLock(2L))
			.willReturn(Optional.of(receiverAccount));

		// when
		depositService.rollbackDeposits();

		// then
		verify(listOperations).remove(eq(FAILED_DEPOSIT), eq(1L), eq(failedDeposit));
		verify(receiverAccount).deposit(1000);
		verify(accountRepository).save(receiverAccount);
	}

	@DisplayName("최대 재시도 횟수 초과 시 출금을 롤백 한다.")
	@Test
	void rollbackDeposits_ExceedMaxRetries() throws Exception {

		// given
		String failedDeposit = "tx1:1:2:1000";
		given(listOperations.range(FAILED_DEPOSIT, 0, -1))
			.willReturn(List.of(failedDeposit));

		given(accountRepository.findByIdWithLock(2L))
			.willThrow(new NotFoundAccountException());

		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.increment("deposit-failures:tx1"))
			.willReturn(6L);

		Account senderAccount = mock(Account.class);
		given(accountRepository.findByIdWithLock(1L))
			.willReturn(Optional.of(senderAccount));
		// when
		depositService.rollbackDeposits();
		// then
		verify(listOperations).remove(eq(FAILED_DEPOSIT), eq(1L), eq(failedDeposit));
		verify(redisTemplate).delete("deposit-failures:tx1");
		verify(senderAccount).deposit(1000);
		verify(accountRepository).save(senderAccount);
	}
}
