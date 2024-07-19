package org.c4marathon.assignment.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    /* 400 Bad Request */
    REQUEST_VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "해당 이메일과 일치하는 사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    DEPOSIT_LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "하루 충전 한도를 초과하였습니다."),
    MAIN_ACCOUNT_BALANCE_EXCEED(HttpStatus.BAD_REQUEST, "메인계좌 잔액이 부족합니다."),
    INVALID_REQUEST_CONTECT(HttpStatus.BAD_REQUEST, "잘못된 데이터를 포함한 요청입니다."),

    /* 401 Unauthorized */
    TOKEN_VALIDATION_EXCEPTION(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    /* 404 Not Found*/


    /* 500 Internal Server Error */
    MAIN_ACCOUNT_PK_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "DB 정합성 문제가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 측에 문제가 생겼습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
