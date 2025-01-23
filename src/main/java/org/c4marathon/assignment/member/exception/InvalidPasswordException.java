package org.c4marathon.assignment.member.exception;

import org.c4marathon.global.exception.CustomException;
import org.c4marathon.global.exception.ErrorCode;

public class InvalidPasswordException extends CustomException {
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}
