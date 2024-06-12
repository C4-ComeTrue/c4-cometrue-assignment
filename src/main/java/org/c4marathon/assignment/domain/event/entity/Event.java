package org.c4marathon.assignment.domain.event.entity;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "event_tbl"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Event extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id", columnDefinition = "BIGINT", updatable = false)
	private Long id;

	@NotNull
	@Column(name = "name", columnDefinition = "VARCHAR(30)")
	private String name;

	@NotNull
	@Column(name = "end_date", columnDefinition = "DATETIME")
	private LocalDateTime endDate;
}
