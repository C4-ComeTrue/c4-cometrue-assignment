package org.c4marathon.assignment.global.util;

import java.security.SecureRandom;
import java.util.stream.IntStream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountNumberUtil {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private  static final int LENGTH = 10;

	public static String generateAccountNumber(String prefix) {
		StringBuilder accountNumber = new StringBuilder(prefix);

		IntStream.range(0, LENGTH).forEach(i -> accountNumber.append(SECURE_RANDOM.nextInt(10)));
		return accountNumber.toString();
	}
}
