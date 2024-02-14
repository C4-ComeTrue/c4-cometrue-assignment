package org.c4marathon.assignment.todo.dto;

import java.sql.Time;
import java.util.Date;

public record EditTodoRequest(Long userId, Long calendarId, Long todoId, String todoName, Date startDay, Time startTime, Date endDay,
                              Time endTime, String todoMemo, Boolean allDayFlag, Boolean repeatFlag) {

    public EditTodoRequest create(Long userId, Long calendarId,
                                  Long todoId, String todoName, Date startDay,
                                  Time startTime, Date endDay, Time endTime,
                                  String todoMemo, Boolean allDayFlag,
                                  Boolean repeatFlag)
    {

        EditTodoRequest editTodoRequest = new EditTodoRequest(userId, calendarId, todoId, todoName, startDay, startTime, endDay,
                endTime, todoMemo, allDayFlag, repeatFlag);
        return editTodoRequest;
    }
}
