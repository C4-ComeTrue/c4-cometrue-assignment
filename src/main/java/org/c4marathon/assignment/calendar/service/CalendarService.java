package org.c4marathon.assignment.calendar.service;

import org.c4marathon.assignment.calendar.entity.Calendar;
import org.c4marathon.assignment.calendar.repository.CalendarRepository;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.user.entity.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarService {

	private final CalendarRepository calendarRepository;
	private final UserRepository userRepository;

	public void createCalendar(Long userId, String calendarName) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> ErrorCode.USER_NOT_EXIST.serviceException("message"));

		// calendar Entity를 생성하고
		Calendar calendar = Calendar.builder()
				.calendarName(calendarName)
				.user(user)
				.build();
		// calendar Repository 에 save
		calendarRepository.save(calendar);

		// user의 calendar에 추가??
		// user Repository save/??

	}

}
