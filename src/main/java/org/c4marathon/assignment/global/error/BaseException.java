package org.c4marathon.assignment.global.error;

import java.io.Serial;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -5436170244551786392L;
	private final String errorCode;
	private final String message;
	private final String debugMessage;

	public BaseException(String errorCode, String message) {
		super(createMessageForm(errorCode, message, null));
		this.errorCode = errorCode;
		this.message = message;
		this.debugMessage = null;
	}

	public BaseException(String errorCode, String message, String debugMessage) {
		super(createMessageForm(errorCode, message, debugMessage));
		this.errorCode = errorCode;
		this.message = message;
		this.debugMessage = debugMessage;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	private static String createMessageForm(String errorCode, String message, String debugMessage) {
		StringBuilder detailMessage = new StringBuilder(errorCode).append(": ").append(message);
		if (debugMessage != null && !debugMessage.isEmpty()) {
			detailMessage.append(", ").append(debugMessage);
		}
		return detailMessage.toString();
	}
}

