package org.c4marathon.assignment.todo.entity;

import java.sql.Time;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor
public class TodoEntity {

	// id
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "todo_id", nullable = false)
	private Long id;

	// 일정 이름
	@Column(name = "todo_name", nullable = false)
	private String todoName;

	// 하루종일 체크 필드
	@Column(name = "todo_allday", nullable = false) // default값이 있는걸로
	private boolean alldayFlag;

	// 시작 날짜
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

	// 관련 메모: text타입
	@Column(name = "todo_memo")
	private String todoMemo;

	// 알림 설정 여부 필드
	@Column(name = "todo_alarm_flag")
	private boolean alarmOnOff;

	// 알림 설정할 경우, 몇 분전 알림인지 필드
	@Column(name = "todo_alarm_time")
	private Time alarmTime;

}
