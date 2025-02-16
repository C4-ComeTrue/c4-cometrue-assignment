package org.c4marathon.assignment.utils;

import java.security.SecureRandom;

public abstract class RandomUtil {
	private static final SecureRandom RANDOM = new SecureRandom();

	public static long getRandom(long end) {
		return RANDOM.nextLong(end);
	}
}
