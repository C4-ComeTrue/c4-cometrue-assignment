package org.c4marathon.assignment.todo.entity;

import java.sql.Time;
import java.util.Date;

import org.c4marathon.assignment.calendar.entity.Calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor
public class Todo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "todo_id", nullable = false)
	private Long id;

	@Column(name = "todo_name", nullable = false)
	private String todoName;

	@Column(name = "todo_start_day", nullable = false)
	private Date startDay;

	// 시작 시간: 분까지만 설정
	@Column(name = "todo_start_time")
	private Time startTime; // HH:MM:SS

	// 끝나는 날짜
	@Column(name = "todo_end_day")
	private Date endDay;

	// 끝나는 시간: 분까지만 설정
	@Column(name = "todo_end_time")
	private Time endTime;

	// 하루종일 체크 필드
	@Column(name = "todo_allday", nullable = false) // default값이 있는걸로
	private boolean allDayFlag;

	// 반복일정 여부
	@Column(name = "todo_repeat")
	private boolean repeatFlag;

	// 관련 메모: text타입
	@Column(name = "todo_memo")
	private String todoMemo;

	// 알림 설정 여부 필드
	@Column(name = "todo_alarm_flag")
	private boolean alarmOnOff;

	// 알림 설정할 경우, 몇 분전 알림인지 필드
	@Column(name = "todo_alarm_time")
	private Time alarmTime;

	@ManyToOne
	@JoinColumn(name = "calendar_id")
	private Calendar calendar;

	@Builder
	public Todo (String todoName, Date startDay, Time startTime, Date endDay, Time endTime, boolean allDayFlag, boolean repeatFlag, String todoMemo,
		boolean alarmOnOff, Time alarmTime, Calendar calendar) {
		this.todoName = todoName;
		this.startDay = startDay;
		this.startTime = startTime;
		this.endDay = endDay;
		this.endTime = endTime;
		this.allDayFlag = allDayFlag;
		this.repeatFlag = repeatFlag;
		this.todoMemo = todoMemo;
		this.alarmOnOff = alarmOnOff;
		this.alarmTime = alarmTime;
		this.calendar = calendar;
	}

}
