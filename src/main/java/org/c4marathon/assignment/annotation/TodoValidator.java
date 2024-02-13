package org.c4marathon.assignment.annotation;

import java.util.Date;

import org.c4marathon.assignment.todo.dto.CreateTodoRequest;
import org.c4marathon.assignment.todo.dto.EditTodoRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TodoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(clazz);
	}

	/**
	 * target의 request타입을 구별하여 날짜순서에 대한 유효성 검사 진행
	 * @param target 유효성 검사 대상
	 * @param errors 유효성 검사 상태정보
	 */
	@Override
	public void validate(Object target, Errors errors) {
		if (target instanceof CreateTodoRequest) {
			CreateTodoRequest dto = (CreateTodoRequest) target;
			checkDate(dto.startDay(), dto.endDay(), errors);
		}
		else if (target instanceof EditTodoRequest) {
			EditTodoRequest dto = (EditTodoRequest) target;
			checkDate(dto.startDay(), dto.endDay(), errors);
		}
	}

	/**
	 * 날짜의 선후관계 검증
	 * @param startDay
	 * @param endDay
	 * @param errors
	 */
	private void checkDate(Date startDay, Date endDay, Errors errors) {
		if (startDay.after(endDay)) {
			errors.rejectValue("startDay", "notEmail", "email 형식으로 입력해주세요.");
		}
	}
}

