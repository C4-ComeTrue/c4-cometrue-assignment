package org.c4marathon.assignment.domain.pointlog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "point_log"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "point_log_id")
	private Long id;

	@Column(name = "consumer_id")
	private Long consumerId;

	@Column(name = "point")
	private Long point;

	public PointLog(Long consumerId, Long point) {
		this.consumerId = consumerId;
		this.point = point;
	}
}
