package org.c4marathon.assignment.global.error;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

	CONSUMER_NEED_ADDRESS(BAD_REQUEST, "소비자인 경우 회원가입 시 주소가 필요합니다."),
	ALREADY_CONSUMER_EXISTS(CONFLICT, "이미 이메일에 해당하는 소비자가 존재합니다."),
	ALREADY_PRODUCT_NAME_EXISTS(CONFLICT, "이미 존재하는 상품명입니다."),
	PRODUCT_NOT_FOUND(NOT_FOUND, "요청에 해당하는 상품이 존재하지 않습니다."),
	NOT_ENOUGH_BALANCE(BAD_REQUEST, "구매자의 충전된 캐시가 부족합니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus httpStatus, String message) {
		this.status = httpStatus;
		this.message = message;
	}
}
