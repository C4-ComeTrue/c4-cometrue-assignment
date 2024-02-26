package org.c4marathon.assignment.member.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {
	private final HttpStatus httpStatus;
	private final String errorName;
	private final String errorMessage;
	private final String debugMessage;

	public MemberException(HttpStatus httpStatus, String errorName, String errorMessage,
		String debugMessage) {
		super(errorMessage);
		this.httpStatus = httpStatus;
		this.errorName = errorName;
		this.errorMessage = errorMessage;
		this.debugMessage = debugMessage;
	}
}
