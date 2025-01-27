package org.c4marathon.assignment.common.exception.enums;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	//404
	NOT_FOUND_MAIN_ACCOUNT(HttpStatus.NOT_FOUND, "메인 계좌가 존재하지 않습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
