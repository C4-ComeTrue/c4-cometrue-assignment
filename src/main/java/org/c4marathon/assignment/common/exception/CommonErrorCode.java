package org.c4marathon.assignment.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
	UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다."),
	INVALID_ARGUMENT_ERROR(HttpStatus.BAD_REQUEST, "올바르지 않은 파라미터입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 잠시 후에 다시 시도해주세요.");

	private final HttpStatus httpStatus;
	private final String message;

	public CommonException commonException() {
		return new CommonException(name(), getHttpStatus(), getMessage());
	}

	public CommonException commonException(String debugMessage) {
		return new CommonException(name(), getHttpStatus(), getMessage(), debugMessage);
	}
}
