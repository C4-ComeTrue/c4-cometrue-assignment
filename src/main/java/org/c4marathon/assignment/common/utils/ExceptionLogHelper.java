package org.c4marathon.assignment.common.utils;

import org.slf4j.Logger;

/**
 * ExceptionHandler에서 예외 처리 시 남길 예외 로그를 처리하는 Helper 클래스
 */
public class ExceptionLogHelper {
	private ExceptionLogHelper() {

	}

	public static void makeExceptionLog(Logger log, Exception error, String errorName) {
		log.error("[{}] error name: {} | message: {}", error.getClass().getSimpleName(), errorName, error.getMessage());
	}

	public static void makeExceptionLog(Logger log, Exception error, String errorName, String debugMessage) {
		log.error("[{}] error name: {} | message: {} | debugMessage: {}", error.getClass().getSimpleName(), errorName,
			error.getMessage(),
			debugMessage);
	}
}

/*
같은 형식의 반복되는 로그들을 하나의 클래스로 관리하고자 작성했습니다.
 */
