package org.c4marathon.assignment.exception;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
	private final String errCode;
	private final String errMessage;
	private final String debugMessage; // 로깅에 굉장히 중요해짐.

	public ServiceException(String errCode, String errMessage, String debugMessage) {
		super(getExceptionMessage(errCode, errMessage, debugMessage));
		this.errCode = errCode;
		this.errMessage = errMessage;
		this.debugMessage = debugMessage;
	}

	public ServiceException(String errCode, String errMessage, String debugMessage, Throwable cause) {
		super(getExceptionMessage(errCode, errMessage, debugMessage), cause);
		this.errCode = errCode;
		this.errMessage = errMessage;
		this.debugMessage = debugMessage;
	}

	public ServiceException(String errCode, String errMessage) {
		super(getExceptionMessage(errCode, errMessage, null));
		this.errCode = errCode;
		this.errMessage = errMessage;
		this.debugMessage = null;
	}

	public ServiceException(String errCode, String errMessage, Throwable cause) {
		super(getExceptionMessage(errCode, errMessage, null), cause);
		this.errCode = errCode;
		this.errMessage = errMessage;
		this.debugMessage = null;
	}

	private static String getExceptionMessage(String errCode, String errMessage, String debugMessage) {
		StringBuilder sb = new StringBuilder();
		sb.append(errCode);
		sb.append(" : ");
		sb.append(errMessage);

		if (!StringUtils.isEmpty(debugMessage)) {
			sb.append(" - ");
			sb.append(debugMessage);
		}

		return sb.toString();
	}
}
