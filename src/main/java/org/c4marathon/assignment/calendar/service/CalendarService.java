package org.c4marathon.assignment.calendar.service;

import java.util.ArrayList;

import org.c4marathon.assignment.calendar.entity.CalendarEntity;
import org.c4marathon.assignment.calendar.repository.CalendarRepository;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.user.entity.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarService {

	private final CalendarRepository calendarRepository;
	private final UserRepository userRepository;

	/**
	 * 캘린더 생성 API
	 * @param userId
	 * @param calendarName
	 */
	@Transactional
	public void createCalendar(Long userId, String calendarName) {
		User user = getUserEntity(userId);

		// calendarName 중복체크
		if (checkDuplicateCalendarName(userId, calendarName)) {
			throw ErrorCode.CAL_NAME_DUPLICATE.serviceException();
		}

		CalendarEntity calendarEntity = new CalendarEntity(calendarName, user, new ArrayList<>());
		calendarRepository.save(calendarEntity);
	}

	private User getUserEntity(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> ErrorCode.USER_NOT_EXIST.serviceException());
	}

	private boolean checkDuplicateCalendarName(Long userId, String calendarName) {
		return calendarRepository.existsByUserIdAndCalendarName(userId, calendarName);
	}
}
