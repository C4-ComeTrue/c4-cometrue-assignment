package org.c4marathon.assignment.settlement.util;

import java.security.SecureRandom;

public class RandomUtils {
	private static SecureRandom secureRandom = new SecureRandom();

	public static long getRandomMoney(long range) {
		return secureRandom.nextLong(range);
	}
}
