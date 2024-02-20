package org.c4marathon.assignment.common.exception;

import java.util.Collections;
import java.util.List;

import org.springframework.validation.FieldError;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

	private String message;

	private List<ErrorDetail> errorsList;

	public ErrorResponse(String message) {
		this.message = message;
		this.errorsList = Collections.emptyList();
	}

	public record ErrorDetail(
		String field,
		Object value,
		String detailMessage
	) {
		public static ErrorDetail from(FieldError ex) {
			return new ErrorDetail(ex.getField(), ex.getRejectedValue(), ex.getDefaultMessage());
		}
	}
}
