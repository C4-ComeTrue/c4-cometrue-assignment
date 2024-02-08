package org.c4marathon.assignment.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	// common
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),
	BAD_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");

	private final HttpStatus status;
	private final String message;

	public BusinessException businessException() {
		return new BusinessException(this.name(), message);
	}

	public BusinessException businessException(Throwable cause) {
		return new BusinessException(cause, this.name(), message);
	}

	public BusinessException businessException(String debugMessage, Object... debugMessageArgs) {
		return new BusinessException(this.name(), message, String.format(debugMessage, debugMessageArgs));
	}

	public BusinessException businessException(Throwable cause, String debugMessage, Object... debugMessageArgs) {
		return new BusinessException(cause, this.name(), message, String.format(debugMessage, debugMessageArgs));
	}

}
