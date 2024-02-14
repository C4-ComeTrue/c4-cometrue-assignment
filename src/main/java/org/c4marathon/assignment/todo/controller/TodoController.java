package org.c4marathon.assignment.todo.controller;

import org.c4marathon.assignment.annotation.TodoValidator;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.todo.dto.CreateTodoRequest;
import org.c4marathon.assignment.todo.dto.DeleteTodoRequest;
import org.c4marathon.assignment.todo.dto.EditTodoRequest;
import org.c4marathon.assignment.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
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

	/**
	 * to-do 생성 API : 유효성 검증 및 수정
	 * @param dto
	 * @param bindingResult
	 */
	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public void createTodo(@Validated @RequestBody CreateTodoRequest dto, BindingResult bindingResult) {

		if (todoValidator.supports(CreateTodoRequest.class)) {
			todoValidator.validate(dto, bindingResult);
		}

		if (bindingResult.hasErrors()) {
			throw ErrorCode.TODO_DATE_VALIDATION.serviceException(ErrorCode.TODO_DATE_VALIDATION.getMessage());
		}
		else {
			todoService.createTodo(
				dto.userId(), dto.calendarId(),
				dto.todoName(), dto.startDay(), dto.startTime(), dto.endDay(), dto.endTime(),
				dto.todoMemo(), dto.allDayFlag(), dto.repeatFlag());
		}
	}

	/**
	 * to-do 수정 API : 유효성 검증 및 수정
	 * @param dto
	 * @param bindingResult
	 */
	 @PatchMapping()
	 @ResponseStatus(HttpStatus.OK)
	 public void editTodo(@Validated @RequestBody EditTodoRequest dto, BindingResult bindingResult) {
	 	if (todoValidator.supports(EditTodoRequest.class)) {
	 		todoValidator.validate(dto, bindingResult);
	 	}

	 	if (bindingResult.hasErrors()) {
	 		throw ErrorCode.TODO_DATE_VALIDATION.serviceException();
	 	}
	 	else {
	 		todoService.editTodo(
	 			dto.userId(), dto.calendarId(), dto.todoId(),
	 			dto.todoName(), dto.startDay(), dto.startTime(), dto.endDay(), dto.endTime(),
	 			dto.todoMemo(), dto.allDayFlag(), dto.repeatFlag());
	 	}
	 }

	/**
	 * to-do 삭제 API
	 * @param dto
	 */
	@DeleteMapping()
	@ResponseStatus(HttpStatus.OK)
	public void deleteTodo(@RequestBody DeleteTodoRequest dto) {
		todoService.deleteTodo(dto.userId(), dto.calendarId(), dto.todoId());
	}
}
