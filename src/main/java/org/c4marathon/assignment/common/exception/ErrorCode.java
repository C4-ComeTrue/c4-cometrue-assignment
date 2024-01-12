package org.c4marathon.assignment.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

	USER_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");

	private final HttpStatus httpStatus;
	private final String message;

	public ServiceException serviceException() {
		return new ServiceException(this.name(), message);
	}

	public ServiceException serviceException(String debugMessage, Object... debugMessageArgs) {
		return new ServiceException(this.name(), message, String.format(debugMessage, debugMessageArgs));
	}

	public ServiceException serviceException(Throwable cause, String debugMessage, Object... debugMessageArgs) {
		return new ServiceException(cause, this.name(), message, String.format(debugMessage, debugMessageArgs));
	}
}
