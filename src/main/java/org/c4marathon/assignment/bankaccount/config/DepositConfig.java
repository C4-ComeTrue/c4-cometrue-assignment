package org.c4marathon.assignment.bankaccount.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class DepositConfig {

	/**
	 *
	 * 메인 계좌 입금 작업을 처리할 스레드 풀입니다.
	 * 스레드 풀 설정은 부하 테스트를 해야 적정 수를 선택할 수 있다고 생각합니다.
	 * 그래서 우선은 적은 수로 설정을 했습니다.
	 */
	@Bean
	public ThreadPoolTaskExecutor depositExecutor() {
		final int corePoolSize = 10;
		final int maxPoolSize = 20;
		final int queueCapacity = 10;
		final boolean completeOnShutdown = true;

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
}
