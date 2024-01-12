package org.c4marathon.assignment.todo.controller;


import org.c4marathon.assignment.annotation.TodoValidator;
import org.c4marathon.assignment.todo.dto.CreateTodoRequest;
import org.c4marathon.assignment.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

	private final TodoService todoService;
	private final TodoValidator todoValidator;

	@InitBinder
	public void init(DataBinder binder) {
		binder.addValidators(todoValidator);
	}

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public void createTodo(@Validated @RequestBody CreateTodoRequest dto, BindingResult bindingResult) {

		if(todoValidator.supports(CreateTodoRequest.class)) {
			todoValidator.validate(dto, bindingResult);
		}

		// TODO : custom exception처리로 변환하기
		if (bindingResult.hasErrors()) {
			System.out.println("Error..");
		}
		else {
			System.out.println("검증 통과! ");
			todoService.createTodo(
				dto.userId(), dto.calendarId(),
				dto.todoName(), dto.startDay(), dto.startTime(), dto.endDay(), dto.endTime(),
				dto.todoMemo(), dto.alarmOnOff(), dto.alarmTime(), dto.allDayFlag(), dto.repeatFlag());
		}
	}
}
