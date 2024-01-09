package org.c4marathon.assignment.util.exceptions;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // ----- Success Response -----
    SUCCESS(OK, "요청에 성공하였습니다."),

    // ----- Login Response -----
    LOGIN_FAILED(EXPECTATION_FAILED, "로그인에 실패했습니다."),

    // ----- Common Responses -----
    COMMON_NOT_FOUND(NOT_FOUND, "정보를 가져오는데 실패하였습니다."),
    COMMON_READ_FAILED(INTERNAL_SERVER_ERROR, "조회에 실패하였습니다."),
    COMMON_CREATE_FAILED(INTERNAL_SERVER_ERROR, "생성에 실패하였습니다."),
    COMMON_DELETE_FAILED(INTERNAL_SERVER_ERROR, "삭제에 실패하였습니다."),
    COMMON_UPDATE_FAILED(INTERNAL_SERVER_ERROR, "수정에 실패하였습니다."),
    NO_REQUIRED_INFORMATION(BAD_REQUEST, "필수 입력 항목이 누락되었습니다."),
    INVALID_INPUT_VALUE(BAD_REQUEST, "올바르지 않은 입력 값입니다."),
    NAME_SUFFIX_LIMIT_EXCEEDED(BAD_REQUEST, "이름의 접미사가 제한 값을 초과하였습니다."),
    UNEXPECTED_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 에러가 발생했습니다."),

    // ----- User-related Responses -----
    DUPLICATED_EMAIL(CONFLICT, "이미 사용 중인 이메일입니다.");


    private final HttpStatus status;
    private final String message;

    public BaseException baseException() {
        return new BaseException(this.name(), message);
    }

    public BaseException baseException(String debugMessage, Object... args) {
        return new BaseException(this.name(), message, String.format(debugMessage, args));
    }
}