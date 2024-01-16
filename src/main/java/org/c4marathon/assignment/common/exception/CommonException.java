package org.c4marathon.assignment.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {
	private final HttpStatus httpStatus;
	private final String errorName;
	private final String errorMessage;
	private final String debugMessage;

	public CommonException(String errorName, HttpStatus httpStatus, String errorMessage) {
		super(errorMessage);
		this.errorName = errorName;
		this.httpStatus = httpStatus;
		this.errorMessage = errorMessage;
		this.debugMessage = null;
	}

	public CommonException(String errorName, HttpStatus httpStatus, String errorMessage,
		String debugMessage) {
		super(errorMessage);
		this.errorName = errorName;
		this.httpStatus = httpStatus;
		this.errorMessage = errorMessage;
		this.debugMessage = debugMessage;
	}
}
