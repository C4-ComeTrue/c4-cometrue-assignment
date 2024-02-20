package org.c4marathon.assignment.common.utils;

public class ChargeLimitUtils {

	private ChargeLimitUtils() {}

	public static final long NO_LIMIT = Long.MAX_VALUE;
	public static final long BASIC_LIMIT = 3000000;
	public static final long MAX_LIMIT = 300000000;

	public static boolean doesExceedLimit(long limitAmount, long totalChargeAmount, long chargeAmount) {
		return limitAmount < totalChargeAmount + chargeAmount;
	}
}
