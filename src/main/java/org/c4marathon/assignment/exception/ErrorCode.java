package org.c4marathon.assignment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "존재하지 않는 회원번호입니다."),
    INVALID_MAIN_ACCOUNT(HttpStatus.BAD_REQUEST, "존재하지 않는 메인 계좌입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid parameter included"),
    EXCEEDED_DEPOSIT_LIMIT(HttpStatus.BAD_REQUEST, "일일 충전 한도를 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
