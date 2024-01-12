package org.c4marathon.assignment.annotation;

import org.c4marathon.assignment.todo.dto.CreateTodoRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TodoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return CreateTodoRequest.class.isAssignableFrom(clazz); // *** ERROR ***
	}

	@Override
	public void validate(Object target, Errors errors) {
		// 내부에서 형변환
		CreateTodoRequest dto = (CreateTodoRequest) target;

		// 검증
		if(dto.startDay().after(dto.endDay())) {
			errors.rejectValue("startDay", "notEmail", "email 형식으로 입력해주세요.");
		}
	}
}

