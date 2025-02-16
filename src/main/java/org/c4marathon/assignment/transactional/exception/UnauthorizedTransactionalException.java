package org.c4marathon.assignment.transactional.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class UnauthorizedTransactionalException extends CustomException {

	public UnauthorizedTransactionalException() {
		super(ErrorCode.UNAUTHORIZED_TRANSACTIONAL);
	}
}
