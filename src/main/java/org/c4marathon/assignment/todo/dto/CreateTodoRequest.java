package org.c4marathon.assignment.todo.dto;

import java.sql.Time;
import java.util.Date;

public record CreateTodoRequest(
	Long userId, Long calendarId,
	String todoName, Date startDay, Time startTime,
	Date endDay, Time endTime, String todoMemo, boolean alarmOnOff,
	Time alarmTime, boolean allDayFlag, boolean repeatFlag
) {

	public static CreateTodoRequest create(
		Long userId, Long calendarId,
		String todoName, Date startDay, Time startTime,
		Date endDay, Time endTime, String todoMemo, boolean alarmOnOff,
		Time alarmTime, boolean allDayFlag, boolean repeatFlag)
	{
		return new CreateTodoRequest(
			userId, calendarId, todoName,
			startDay, startTime, endDay, endTime,
			todoMemo,
			alarmOnOff, alarmTime,
			allDayFlag, repeatFlag);
	}
}
