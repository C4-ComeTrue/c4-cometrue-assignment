package org.c4marathon.assignment.common.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class EncryptUtils {

	public static String encrypt(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}

	public static boolean check(String password, String hashValue) {
		return BCrypt.checkpw(password, hashValue);
	}
}
