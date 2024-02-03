package org.c4marathon.assignment.common.response;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 *
 * @param errors validation 검사에서 적발된 예외 List
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(HttpStatus httpStatus, int code, String message, List<ValidationError> errors) {
	public static ErrorResponse of(HttpStatus httpStatus, String message) {
		return ErrorResponse.builder().httpStatus(httpStatus).code(httpStatus.value()).message(message).build();
	}

	public static ErrorResponse of(HttpStatus httpStatus, String message, BindingResult bindingResult) {
		return ErrorResponse.builder()
			.httpStatus(httpStatus)
			.code(httpStatus.value())
			.message(message)
			.errors(ValidationError.of(bindingResult))
			.build();
	}

	@Getter
	public static class ValidationError {
		private final String field;
		private final String value;

		private final String message;

		private ValidationError(FieldError fieldError) {
			this.field = fieldError.getField();
			Object rejectedValue = fieldError.getRejectedValue();
			this.value = rejectedValue == null ? "" : rejectedValue.toString();
			this.message = fieldError.getDefaultMessage();
		}

		public static List<ValidationError> of(final BindingResult bindingResult) {
			return bindingResult.getFieldErrors().stream().map(ValidationError::new).toList();
		}
	}
}
