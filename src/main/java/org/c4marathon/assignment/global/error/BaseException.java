package org.c4marathon.assignment.global.error;

import java.io.Serial;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -5436170244551786392L;
	private final ErrorCode errorCode;
	private final String message;

	public BaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.message = errorCode.getMessage();
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}

