package org.c4marathon.assignment.settlement.util;

import java.security.SecureRandom;

public class SettlementUtils {
	public static final int PAGE_SIZE = 10;
	public static final long MIN_UNIT = 100; // 랜덤으로 나누는 돈의 최소 단위
	private static SecureRandom secureRandom = new SecureRandom();

	private SettlementUtils() {
	}

	public static long getRandomMoney(long range) {
		return secureRandom.nextLong(range);
	}
}
