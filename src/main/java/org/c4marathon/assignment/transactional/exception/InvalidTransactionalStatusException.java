package org.c4marathon.assignment.transactional.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class InvalidTransactionalStatusException extends CustomException {

	public InvalidTransactionalStatusException() {
		super(ErrorCode.INVALID_TRANSACTIONAL_STATUS);
	}
}
