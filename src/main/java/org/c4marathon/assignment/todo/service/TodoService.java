package org.c4marathon.assignment.todo.service;

import java.sql.Time;
import java.util.Date;

import org.c4marathon.assignment.calendar.entity.Calendar;
import org.c4marathon.assignment.calendar.repository.CalendarRepository;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.todo.entity.Todo;
import org.c4marathon.assignment.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;

@Validated
@Service
@RequiredArgsConstructor
public class TodoService {

	private final TodoRepository todoRepository;
	private final CalendarRepository calendarRepository;

	public void createTodo(Long userId, Long calendarId, String todoName, Date startDay, Time startTime, Date endDay, Time endTime,
		String todoMemo, boolean alarmOnOff, Time alarmTime, boolean allDayFlag, boolean repeatFlag) {

		// userId랑 calendrId이용해서 calendarEntity가져오기
		Calendar calendar = getCalendarEntity(userId, calendarId);

		Todo todo = Todo.builder()
			.todoName(todoName)
			.startDay(startDay).startTime(startTime).endDay(endDay).endTime(endTime)
			.allDayFlag(allDayFlag)
			.todoMemo(todoMemo)
			.alarmOnOff(alarmOnOff).alarmTime(alarmTime)
			.repeatFlag(repeatFlag)
			.calendar(calendar)
			.build();

		// 실제 db에 반영하기
		todoRepository.save(todo);
	}

	// get calenadr Entity
	private Calendar getCalendarEntity(Long userId, Long calendarId) {

		return calendarRepository.findByUserIdAndId(userId, calendarId)
			.orElseThrow(() -> ErrorCode.USER_NOT_EXIST.serviceException("message"));
	}

}
