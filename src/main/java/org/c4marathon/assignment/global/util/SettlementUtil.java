package org.c4marathon.assignment.global.util;

import java.security.SecureRandom;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SettlementUtil {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public static int getRandomInt(int range) {
		return SECURE_RANDOM.nextInt(range);
	}

}
