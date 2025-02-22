package org.c4marathon.assignment.account.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class NotFoundSavingProductException extends CustomException {
	public NotFoundSavingProductException() {
		super(ErrorCode.NOT_FOUND_SAVING_PRODUCT);
	}
}
