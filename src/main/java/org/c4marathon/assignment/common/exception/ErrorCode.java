package org.c4marathon.assignment.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

	// User
	USER_SIGN_UP_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "회원가입 형식에 맞지 않습니다."),
	USER_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	USER_PASSWORD_MATCH_ERROR(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),

	// Calendar
	CAL_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 캘린더입니다."),
	CAL_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "같은 캘린더 이름이 존재합니다."),


	// To-Do
	TODO_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다."),


	// Validation
	TODO_DATE_VALIDATION(HttpStatus.UNPROCESSABLE_ENTITY, "시작날짜와 끝나는 날짜의 순서를 맞춰주세요.");

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
