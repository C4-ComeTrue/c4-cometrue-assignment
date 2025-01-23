package org.c4marathon.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Member
    DUPLICATE_EMAIL(400, "이미 가입한 이메일입니다."),
    NOT_FOUND_MEMBER(404, "조회된 멤버가 없습니다."),
    INVALID_PASSWORD(400, "잘못된 비밀번호 입니다.");

    private final int status;
    private final String message;
}
