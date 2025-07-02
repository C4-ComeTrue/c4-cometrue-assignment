package org.c4marathon.assignment.common.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	@Override
	@Bean("customTaskExecutor")
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);               // 기본 스레드 수
		executor.setMaxPoolSize(10);               // 최대 스레드 수
		executor.setQueueCapacity(100);            // 큐 x용량
		executor.setWaitForTasksToCompleteOnShutdown(true); // 종료 시 대기 여부
		executor.setAwaitTerminationSeconds(30);   // 종료 대기 시간
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (ex, method, params) -> {
			System.err.println("[@Async ERROR] Method: " + method.getName());
			ex.printStackTrace();
		};
	}
}
