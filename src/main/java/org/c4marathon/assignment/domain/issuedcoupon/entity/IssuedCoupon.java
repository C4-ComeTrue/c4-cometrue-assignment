package org.c4marathon.assignment.domain.issuedcoupon.entity;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "issued_coupon_tbl"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class IssuedCoupon extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "issued_coupon_id", columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "coupon_id", columnDefinition = "BIGINT")
	private Long couponId;

	@NotNull
	@Column(name = "used_count", columnDefinition = "INTEGER")
	private Integer usedCount;

	@NotNull
	@Column(name = "consumer_id", columnDefinition = "BIGINT")
	private Long consumerId;

	@Builder
	public IssuedCoupon(Long couponId, Long consumerId) {
		this.couponId = couponId;
		this.usedCount = 0;
		this.consumerId = consumerId;
	}

	public void validatePermission(Long targetId) {
		if (!consumerId.equals(targetId)) {
			throw NO_PERMISSION.baseException();
		}
	}

	public void updateUsedCount() {
		usedCount++;
	}
}
