package org.c4marathon.assignment.member.exception;

import org.c4marathon.global.exception.CustomException;
import org.c4marathon.global.exception.ErrorCode;

public class NotFoundMemberException extends CustomException {
    public NotFoundMemberException() {
        super(ErrorCode.NOT_FOUND_MEMBER);
    }
}
