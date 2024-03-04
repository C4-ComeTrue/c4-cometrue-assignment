package org.c4marathon.assignment.domain.pointlog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
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

	@Column(name = "earned_point")
	private Long earnedPoint;

	@Column(name = "used_point")
	private Long usedPoint;

	@Column(name = "total_amount")
	private Long totalAmount;

	@Column(name = "is_confirm")
	private Boolean isConfirm;

	@Builder
	public PointLog(Long consumerId, Long earnedPoint, Long usedPoint, Long totalAmount, Boolean isConfirm) {
		this.consumerId = consumerId;
		this.earnedPoint = earnedPoint;
		this.usedPoint = usedPoint;
		this.totalAmount = totalAmount;
		this.isConfirm = isConfirm;
	}
}
