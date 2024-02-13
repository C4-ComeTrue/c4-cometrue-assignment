package org.c4marathon.assignment.todo.service;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import org.c4marathon.assignment.calendar.entity.CalendarEntity;
import org.c4marathon.assignment.calendar.repository.CalendarRepository;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.todo.entity.Todo;
import org.c4marathon.assignment.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;

@Validated
@Service
@RequiredArgsConstructor
public class TodoService {

	private final TodoRepository todoRepository;
	private final CalendarRepository calendarRepository;

	/**
	 * To-Do 생성 API
	 * @param userId
	 * @param calendarId
	 * @param todoName
	 * @param startDay
	 * @param startTime
	 * @param endDay
	 * @param endTime
	 * @param todoMemo
	 * @param allDayFlag
	 * @param repeatFlag
	 */
	@Transactional
	public void createTodo(Long userId, Long calendarId, String todoName, Date startDay, Time startTime, Date endDay, Time endTime,
		String todoMemo, boolean allDayFlag, boolean repeatFlag) {
		CalendarEntity calendarEntity = getCalendarEntity(userId, calendarId);

		Todo todo = new Todo(todoName, startDay, startTime, endDay, endTime, allDayFlag, todoMemo, repeatFlag, calendarEntity);
		calendarEntity.addTodo(todo);

		todoRepository.save(todo);
	}

	/**
	 * To-Do 수정 API
	 * @param userId
	 * @param calendarId
	 * @param todoId
	 * @param todoName
	 * @param startDay
	 * @param startTime
	 * @param endDay
	 * @param endTime
	 * @param todoMemo
	 * @param allDayFlag
	 * @param repeatFlag
	 */
	@Transactional
	public void editTodo(Long userId, Long calendarId, Long todoId, String todoName, Date startDay, Time startTime, Date endDay, Time endTime,
		String todoMemo, boolean allDayFlag, boolean repeatFlag) {

		CalendarEntity calendarEntity = getCalendarEntity(userId, calendarId);
		Todo todo = getTodoEntity(calendarEntity, todoId);

		todo.setTodoName(todoName);
		todo.setStartDay(startDay);
		todo.setStartTime(startTime);
		todo.setEndDay(endDay);
		todo.setEndTime(endTime);
		todo.setTodoMemo(todoMemo);
		todo.setAllDayFlag(allDayFlag);
		todo.setRepeatFlag(repeatFlag);
	}

	/**
	 * To-do 삭제 API
	 * @param userId
	 * @param calendarId
	 * @param todoId
	 */
	public void deleteTodo(Long userId, Long calendarId, Long todoId) {
		CalendarEntity calendarEntity = getCalendarEntity(userId, calendarId);
		Todo todo = getTodoEntity(calendarEntity, todoId);

		todoRepository.delete(todo);
	}

	@Transactional
	public Todo getTodoEntity(CalendarEntity calendarEntity, Long todoId) {
		List<Todo> todos = todoRepository.findAllByCalendarEntity(calendarEntity);

		for(Todo todo : todos) {
			if (todo.getId() == todoId) {
				return todo;
			}
		}
		throw ErrorCode.TODO_NOT_EXIST.serviceException();
	}

	public CalendarEntity getCalendarEntity(Long userId, Long calendarId) {
		return calendarRepository.findByUserIdAndId(userId, calendarId)
			.orElseThrow(() -> ErrorCode.CAL_NOT_EXIST.serviceException());
	}
}
