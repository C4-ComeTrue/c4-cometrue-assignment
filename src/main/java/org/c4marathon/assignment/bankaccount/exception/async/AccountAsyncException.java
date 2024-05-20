package org.c4marathon.assignment.bankaccount.exception.async;

import lombok.Getter;

@Getter
public class AccountAsyncException extends RuntimeException {
	private final String errorName;
	private final String message;
	private final Exception exception;

	public AccountAsyncException(String errorName, String message) {
		this.errorName = errorName;
		this.message = message;
		this.exception = null;
	}
}
