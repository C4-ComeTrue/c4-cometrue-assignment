package org.c4marathon.assignment.calendar.controller;

import org.c4marathon.assignment.calendar.dto.CreateCalendarRequest;
import org.c4marathon.assignment.calendar.service.CalendarService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarEntityController {

	private final CalendarService  calendarService;

	/**
	 * 캘린더 생성 API
	 * @param dto
	 */
	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public void createCalendar(@RequestBody CreateCalendarRequest dto) {
		calendarService.createCalendar(dto.userId(), dto.calendarName());
	}
}

