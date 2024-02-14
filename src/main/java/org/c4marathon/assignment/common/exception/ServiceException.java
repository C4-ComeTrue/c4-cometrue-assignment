package org.c4marathon.assignment.common.exception;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException{

	private final String errorCode;
	private final String errorMessage;
	private final String debugMessage;

	public ServiceException(String errorCode, String errorMessage) {
		super(getDetailExceptionMessage(errorCode, errorMessage, null));
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.debugMessage = null;
	}

	public ServiceException(String errorCode, String errorMessage, String debugMessage) {
		super(getDetailExceptionMessage(errorCode, errorMessage, debugMessage));
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.debugMessage = debugMessage;
	}

	public ServiceException(Throwable cause, String errorCode, String errorMessage, String debugMessage) {
		super(getDetailExceptionMessage(errorCode, errorMessage, debugMessage), cause);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.debugMessage = debugMessage;
	}

	private static String getDetailExceptionMessage(String errorCode, String errorMessage, String debugMessage) {
		var sb = new StringBuilder()
			.append(errorCode)
			.append(" : ")
			.append(errorMessage);

		if (StringUtils.isNotEmpty(debugMessage)) {
			sb.append("-")
				.append(debugMessage);
		}
		return sb.toString();
	}
}
