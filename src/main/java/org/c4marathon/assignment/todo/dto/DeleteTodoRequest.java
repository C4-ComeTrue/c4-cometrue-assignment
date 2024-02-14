package org.c4marathon.assignment.todo.dto;

public record DeleteTodoRequest(Long userId, Long calendarId, Long todoId) {

	public DeleteTodoRequest create(Long userId, Long calendarId, Long todoId) {
		return new DeleteTodoRequest(userId, calendarId, todoId);
	}
}
