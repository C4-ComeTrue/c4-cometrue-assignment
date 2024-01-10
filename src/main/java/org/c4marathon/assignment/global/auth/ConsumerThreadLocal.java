package org.c4marathon.assignment.global.auth;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumerThreadLocal {

	private static final ThreadLocal<Consumer> CONSUMER_THREAD_LOCAL;

	static {
		CONSUMER_THREAD_LOCAL = new ThreadLocal<>();
	}

	public static void set(Consumer consumer) {
		CONSUMER_THREAD_LOCAL.set(consumer);
	}

	public static void remove() {
		CONSUMER_THREAD_LOCAL.remove();
	}

	public static Consumer get() {
		return CONSUMER_THREAD_LOCAL.get();
	}
}
