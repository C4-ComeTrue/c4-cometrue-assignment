package org.c4marathon.assignment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
	public static final String ASYNC_LISTENER_TASK_EXECUTOR_NAME = "AsyncListenerTaskExecutor";
	private static final int CORE_POOL_SIZE = 2;
	private static final int MAX_POOL_SIZE = 4;

	@Bean(name = ASYNC_LISTENER_TASK_EXECUTOR_NAME)
	public ThreadPoolTaskExecutor asyncListenerTaskExecutor() {
		return getThreadPoolTaskExecutor(ASYNC_LISTENER_TASK_EXECUTOR_NAME);
	}
	private ThreadPoolTaskExecutor getThreadPoolTaskExecutor(String asyncSchedulerTaskExecutorName) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(CORE_POOL_SIZE);
		executor.setMaxPoolSize(MAX_POOL_SIZE);
		executor.setThreadNamePrefix(asyncSchedulerTaskExecutorName);
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(10);
		executor.initialize();
		return executor;
	}
}
