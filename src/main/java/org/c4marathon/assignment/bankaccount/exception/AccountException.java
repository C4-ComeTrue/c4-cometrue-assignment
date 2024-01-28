package org.c4marathon.assignment.bankaccount.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AccountException extends RuntimeException {
	private final HttpStatus httpStatus;
	private final String errorName;
	private final String errorMessage;
	private final String debugMessage;

	public AccountException(HttpStatus httpStatus, String errorName, String errorMessage,
		String debugMessage) {
		super(errorMessage);
		this.httpStatus = httpStatus;
		this.errorName = errorName;
		this.errorMessage = errorMessage;
		this.debugMessage = debugMessage;
	}
}
