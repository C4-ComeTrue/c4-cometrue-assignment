package org.c4marathon.assignment.member.exception;

import org.c4marathon.global.exception.CustomException;
import org.c4marathon.global.exception.ErrorCode;

public class DuplicateEmailException extends CustomException {

    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
}
