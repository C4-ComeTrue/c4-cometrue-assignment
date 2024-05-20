package org.c4marathon.assignment.bankaccount.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.c4marathon.assignment.bankaccount.exception.async.AccountAsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class DepositConfig implements AsyncConfigurer {

	@Value("${bank-account.deposit.core-pool-size}")
	private int corePoolSize;
	@Value("${bank-account.deposit.max-pool-size}")
	private int maxPoolSize;
	@Value("${bank-account.deposit.queue-capacity}")
	private int queueCapacity;
	@Value("${bank-account.deposit.complete-on-shutdown}")
	private boolean completeOnShutdown;

	/**
	 *
	 * 메인 계좌 입금 작업을 처리할 스레드 풀입니다.
	 * 스레드 풀 설정은 부하 테스트를 해야 적정 수를 선택할 수 있다고 생각합니다.
	 * 그래서 우선은 적은 수로 설정을 했습니다.
	 */
	@Bean
	public ThreadPoolTaskExecutor depositExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize); // 기본 스레드 수
		executor.setMaxPoolSize(maxPoolSize); // 큐에 대기하는 스레드가 가득차면 최대로 생성할 스레드 수
		executor.setQueueCapacity(queueCapacity); // 큐에 대기하는 스레드의 수
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 스레드 풀에게 요청한 스레드에서 예외 처리
		executor.setWaitForTasksToCompleteOnShutdown(completeOnShutdown);
		executor.setThreadNamePrefix("DP_THREAD_");
		executor.initialize();

		return executor;
	}

	@Bean
	public ThreadPoolTaskExecutor rollbackExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setWaitForTasksToCompleteOnShutdown(completeOnShutdown);
		executor.setThreadNamePrefix("RB_THREAD_");
		executor.initialize();

		return executor;
	}

	/**
	 *
	 * doDeposit() 메소드 예외 처리 핸들러
	 */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new AccountAsyncExceptionHandler();
	}
}
