package org.c4marathon.assignment.common.exception;

public record ErrorResponse(
	String errorCode,
	String errorMessage
) {

	public static ErrorResponse create(ServiceException exception) {
		return new ErrorResponse(exception.getErrorCode(), exception.getErrorMessage());
	}
}
