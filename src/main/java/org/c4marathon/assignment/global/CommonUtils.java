package org.c4marathon.assignment.global;

import java.security.SecureRandom;

import lombok.Getter;

public abstract class CommonUtils {
	private static final SecureRandom RANDOM = new SecureRandom();

	public static long getCeil(long val, long ceilingPoint) {
		long mul = (val + ceilingPoint - 1) / ceilingPoint;

		return ceilingPoint * mul;
	}

	public static long getRandom(long start, long end) {
		return RANDOM.nextLong(start, end);
	}

	public static int getRandom(int start, int end) {
		return RANDOM.nextInt(start, end);
	}

	@Getter
	public static class Pair<T, R> {
		private T t;
		private R r;

		private Pair(T t, R r) {
			this.t = t;
			this.r = r;
		}

		public void update(T t, R r) {
			this.t = t;
			this.r = r;
		}

		public static <T, R> Pair<T, R> of(T t, R r) {
			return new Pair<>(t, r);
		}
	}
}
