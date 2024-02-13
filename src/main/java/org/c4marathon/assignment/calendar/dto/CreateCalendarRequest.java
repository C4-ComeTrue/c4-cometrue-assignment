package org.c4marathon.assignment.calendar.dto;

public record CreateCalendarRequest(Long userId, String calendarName) {

	public static CreateCalendarRequest create (Long userId, String calendarName) {
		return new CreateCalendarRequest(userId, calendarName);
	}
}
