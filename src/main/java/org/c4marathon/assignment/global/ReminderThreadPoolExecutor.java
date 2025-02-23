package org.c4marathon.assignment.global;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReminderThreadPoolExecutor implements Executor {
	private static final int MIN_THREAD_SIZE = 16;
	private static final int MAX_THREAD_SIZE = 32;
	private static final int MAX_BLOCKING_NUM = 2000;
	private static final int MAX_TRY_LIMIT = 3;

	private ThreadPoolExecutor threadPool;

	public ReminderThreadPoolExecutor() {}

	public void init() {
		if (threadPool != null) {
			log.warn("threadPool is not null");
			return;
		}

		threadPool = new ThreadPoolExecutor(MIN_THREAD_SIZE, MAX_THREAD_SIZE, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>(MAX_BLOCKING_NUM));
	}

	@Override
	public void execute(@NonNull Runnable r) {
		if (isInvalid())
			return;

		int tryCount = 0;

		while (tryCount < MAX_TRY_LIMIT) {
			try {
				threadPool.execute(r);

				return;
			} catch (RuntimeException e) {
				tryCount++;
			}
		}
	}

	public void shutdown() {
		if (isInvalid())
			return;

		threadPool.shutdown();
		try {
			threadPool.awaitTermination(100000L, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error(e.toString(), e);
			Thread.currentThread().interrupt();
		}
	}

	private boolean isInvalid() {
		return threadPool == null || threadPool.isTerminated() || threadPool.isTerminating();
	}

}
