package org.c4marathon.assignment.common.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class ErrorCodeTest {

	@Test
	void noArgumentConstructor() {
		var exception = ErrorCode.INVALID_ACCOUNT.businessException();
		assertEquals("INVALID_ACCOUNT", exception.getErrorCode());
		assertEquals("존재하지 않는 계좌입니다.", exception.getErrorMessage());
	}

	@Test
	void debugMessageConstructor() {
		var exception = ErrorCode.INVALID_ACCOUNT.businessException("invalid account : %d", 1);
		assertEquals("INVALID_ACCOUNT", exception.getErrorCode());
		assertEquals("존재하지 않는 계좌입니다.", exception.getErrorMessage());
		assertEquals("invalid account : 1", exception.getDebugMessage());
	}

	@Test
	void throwConstructor() {
		var cause = mock(Throwable.class);
		var exception = ErrorCode.INVALID_ACCOUNT.businessException(cause);
		assertEquals("INVALID_ACCOUNT", exception.getErrorCode());
		assertEquals("존재하지 않는 계좌입니다.", exception.getErrorMessage());
	}

	@Test
	void throwAndDebugConstructor() {
		var cause = mock(Throwable.class);
		var exception = ErrorCode.INVALID_ACCOUNT.businessException(cause, "invalid account : %d", 1);
		assertEquals("INVALID_ACCOUNT", exception.getErrorCode());
		assertEquals("존재하지 않는 계좌입니다.", exception.getErrorMessage());
		assertEquals("invalid account : 1", exception.getDebugMessage());
	}
}
