package org.c4marathon.assignment.member.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
	USER_ALREADY_EXIST(HttpStatus.CONFLICT, "해당 아이디는 이미 사용중입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
	private final HttpStatus httpStatus;
	private final String message;

	public MemberException memberException(String debugMessage) {
		return new MemberException(getHttpStatus(), name(), getMessage(), debugMessage);
	}
}
