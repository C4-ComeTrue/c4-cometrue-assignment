package org.c4marathon.assignment.global.model;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.c4marathon.assignment.global.util.PageTokenUtil;

/**
 * Cursor 정보
 * @param pageToken
 * @param data
 * @param hasNext
 * @param <T>
 */
public record PageInfo<T>(
	String pageToken,
	List<T> data,
	boolean hasNext
) {
	public static <T> PageInfo<T> of(
		List<T> data,
		int expectedSize,
		Function<T, Object> firstPageTokenFunction,
		Function<T, Object> secondPageTokenFunction
	) {
		if (data.size() <= expectedSize) {
			return new PageInfo<>(null, data, false);
		}

		var lastValue = data.get(expectedSize - 1);
		var pageToken = PageTokenUtil.encodePageToken(Pair.of(
			firstPageTokenFunction.apply(lastValue),
			secondPageTokenFunction.apply(lastValue)
		));

		return new PageInfo<>(pageToken, data.subList(0, expectedSize), true);
	}
}
