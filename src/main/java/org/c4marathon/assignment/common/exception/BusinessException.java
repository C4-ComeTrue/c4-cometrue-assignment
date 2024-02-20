package org.c4marathon.assignment.common.exception;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private final String errorCode;

	private final String errorMessage;

	private final String debugMessage;

	public BusinessException(String errorCode, String message) {
		super(getFullExceptionMessage(errorCode, message, null));
		this.errorCode = errorCode;
		this.errorMessage = message;
		this.debugMessage = null;
	}

	public BusinessException(Throwable cause, String errorCode, String message) {
		super(getFullExceptionMessage(errorCode, message, null), cause);
		this.errorCode = errorCode;
		this.errorMessage = message;
		this.debugMessage = null;
	}

	public BusinessException(String errorCode, String message, String debugMessage) {
		super(getFullExceptionMessage(errorCode, message, null));
		this.errorCode = errorCode;
		this.errorMessage = message;
		this.debugMessage = debugMessage;
	}

	public BusinessException(Throwable cause, String errorCode, String message, String debugMessage) {
		super(getFullExceptionMessage(errorCode, message, null), cause);
		this.errorCode = errorCode;
		this.errorMessage = message;
		this.debugMessage = debugMessage;
	}

	private static String getFullExceptionMessage(String errorCode, String errorMessage, String debugMessage) {
		var sb = new StringBuilder()
			.append(errorCode)
			.append(" : ")
			.append(errorMessage);

		if (StringUtils.isNotEmpty(debugMessage)) {
			sb.append(" - ").append(debugMessage);
		}

		return sb.toString();
	}
}
