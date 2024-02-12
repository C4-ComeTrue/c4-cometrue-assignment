package org.c4marathon.assignment.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	// common
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),
	BAD_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

	// member
	INVALID_MEMBER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

	// account
	INVALID_ACCOUNT(HttpStatus.NOT_FOUND, "존재하지 않는 계좌입니다."),
	EXCEED_CHARGE_LIMIT(HttpStatus.BAD_REQUEST, "1일 충전 한도를 넘어 충전이 불가능합니다.");

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
