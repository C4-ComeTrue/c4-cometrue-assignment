package org.c4marathon.assignment.global.core;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MiniPayThreadPoolExecutor implements Executor {

	private final int threadCount;
	private final int queueSize;
	private ThreadPoolExecutor threadPoolExecutor;

	public void init() {
		if (threadPoolExecutor != null) {
			return;
		}
		threadPoolExecutor = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>(queueSize),
			(r, executor) -> {
				try {
					executor.getQueue().put(r);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
	}

	@Override
	public void execute(Runnable command) {
		if (isInvalidState()) {
			return;
		}
		threadPoolExecutor.execute(() -> {
			try {
				command.run();
			} catch (RuntimeException e) {
				log.error(e.toString(), e);
			}
		});
	}

	public void waitToEnd() {
		if (isInvalidState()) {
			return;
		}

		threadPoolExecutor.shutdown();
		while (true) {
			try {
				if (threadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				log.error(e.toString(), e);
				Thread.currentThread().interrupt();
			}
		}
		threadPoolExecutor = null;

	}

	private boolean isInvalidState() {
		return threadPoolExecutor == null || threadPoolExecutor.isTerminating() || threadPoolExecutor.isTerminated();
	}
}
