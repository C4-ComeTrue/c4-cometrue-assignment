package org.c4marathon.assignment.util.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    // ----- Success Response -----
    SUCCESS(true, HttpStatus.OK.value(), "요청에 성공하였습니다."),

    // ----- Login Response -----
    LOGIN_FAILED(false, HttpStatus.EXPECTATION_FAILED.value(), "로그인에 실패했습니다."),

    // ----- Common Responses -----
    COMMON_NOT_FOUND(false, HttpStatus.NOT_FOUND.value(), "정보를 가져오는데 실패하였습니다."),
    COMMON_READ_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "조회에 실패하였습니다."),
    COMMON_CREATE_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "생성에 실패하였습니다."),
    COMMON_DELETE_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "삭제에 실패하였습니다."),
    COMMON_UPDATE_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "수정에 실패하였습니다."),
    NO_REQUIRED_INFORMATION(false, HttpStatus.BAD_REQUEST.value(), "필수 입력 항목이 누락되었습니다."),
    INVALID_INPUT_VALUE(false, HttpStatus.BAD_REQUEST.value(), "올바르지 않은 입력 값입니다."),
    NAME_SUFFIX_LIMIT_EXCEEDED(false, HttpStatus.BAD_REQUEST.value(), "이름의 접미사가 제한 값을 초과하였습니다."),
    UNEXPECTED_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "예상치 못한 에러가 발생했습니다."),

    // ----- User-related Responses -----
    DUPLICATED_EMAIL(false, HttpStatus.CONFLICT.value(), "이미 사용 중인 이메일입니다.");


    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}