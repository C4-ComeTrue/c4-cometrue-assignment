package org.c4marathon.assignment.transactional.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class NotFoundTransactionalException extends CustomException {
	public NotFoundTransactionalException() {
		super(ErrorCode.NOT_FOUND_TRANSACTIONAL);
	}
}
