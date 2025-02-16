package org.c4marathon.assignment.transactional.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class UnauthorizedTransactionException extends CustomException {

	public UnauthorizedTransactionException() {
		super(ErrorCode.UNAUTHORIZED_TRANSACTION);
	}
}
