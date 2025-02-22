package org.c4marathon.assignment.global;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class QueryTemplate {

	public static <T> void selectAndExecuteWithCursor(Function<T, List<T>> selectFunc, Consumer<List<T>> resultConsumer, int limit) {
		List<T> resList = null;

		do {
			resList = selectFunc.apply(resList == null ? null : resList.get(resList.size() - 1));

			if (!resList.isEmpty()) {
				resultConsumer.accept(resList);
			}

		} while (resList.size() >= limit);
	}
}
