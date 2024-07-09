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

    /* 401 Unauthorized */

    /* 404 Not Found*/


    /* 500 Internal Server Error */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "");

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
