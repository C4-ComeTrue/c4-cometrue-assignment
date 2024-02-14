package org.c4marathon.assignment.todo.entity;

import java.sql.Time;
import java.util.Date;

import jakarta.persistence.FetchType;
import org.c4marathon.assignment.calendar.entity.CalendarEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "todo_id")
	private Long id;

	@Size(max=50)
	@Column(name = "todo_name")
	private String todoName;

	@Column(name = "todo_start_day")
	private Date startDay;

	@Column(name = "todo_start_time")
	private Time startTime;

	@Column(name = "todo_end_day")
	private Date endDay;

	@Column(name = "todo_end_time")
	private Time endTime;

	@NotNull
	@Column(name = "todo_all_day_flag")
	private boolean allDayFlag;

	@NotNull
	@Column(name = "todo_repeat_flag")
	private boolean repeatFlag;

	@NotNull
	@Size(max=50)
	@Column(name = "todo_memo")
	private String todoMemo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calendar_id")
	private CalendarEntity calendarEntity;

	public Todo(String todoName, Date startDay, Time startTime, Date endDay, Time endTime, boolean allDayFlag, String todoMemo, boolean repeatFlag, CalendarEntity calendarEntity) {
	}

	public void editTodoName(String todoName) {
		this.todoName = todoName;
	}

	public void editStartDay(Date editStartDay) {
		this.startDay = editStartDay;
	}

	public void editStartTime(Time editStartTime) {
		this.startTime = editStartTime;
	}

	public void editEndDay(Date editEndDay) {
		this.endDay = editEndDay;
	}

	public void editEndTime(Time editEndTime) {
		this.endTime = editEndTime;
	}

	public void editAllDayFlag(boolean editAllDayFlag) {
		this.allDayFlag = editAllDayFlag;
	}

	public void editRepeatFlag(boolean editRepeatFlag) {
		this.repeatFlag = editRepeatFlag;
	}

	public void editTodoMemo(String editTodoMemo) {
		this.todoMemo = editTodoMemo;
	}

}
