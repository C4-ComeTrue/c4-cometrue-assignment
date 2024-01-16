package org.c4marathon.assignment.member.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
	USER_ALREADY_EXIST(HttpStatus.CONFLICT, "해당 아이디는 이미 사용중입니다.");
	private final HttpStatus httpStatus;
	private final String message;

	public MemberException memberException() {
		return new MemberException(getHttpStatus(), name(), getMessage());
	}

	public MemberException memberException(String debugMessage) {
		return new MemberException(getHttpStatus(), name(), getMessage(), debugMessage);
	}
}
