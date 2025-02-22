package org.c4marathon.assignment.global;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class QueryTemplate {
	private static final int MAX_RETRY_COUNT = 3;
	private static final Logger log = LoggerFactory.getLogger(QueryTemplate.class);

	public static <T> void selectAndExecuteWithCursor(Supplier<List<T>> selectSupplier, Consumer<List<T>> resultConsumer,
		int limit) {
		List<T> resList;

		do {
			resList = selectSupplier.get();

			if (!resList.isEmpty()) {
				resultConsumer.accept(resList);
			}

		} while (resList.size() >= limit);
	}

	public static <T> void selectAndExecuteWithCursorAndTx(PlatformTransactionManager txManager,
		Function<T, List<T>> selectFunc, Consumer<List<T>> resultConsumer, int limit) {
		List<T> resList = null;

		do {
			int retryCount = 0;

			while (retryCount < MAX_RETRY_COUNT) {
				TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

				try {
					if (retryCount == 0) {
						resList = selectFunc.apply(resList == null ? null : resList.get(resList.size() - 1));
					}

					if (!resList.isEmpty()) {
						resultConsumer.accept(resList);
					}

					txManager.commit(status);
					break;
				} catch (RuntimeException e) {
					txManager.rollback(status);
					log.error(e.getMessage());
					retryCount++;
				}
			}

			if (retryCount >= MAX_RETRY_COUNT)
				return;

		} while (resList.size() >= limit);
	}
}
