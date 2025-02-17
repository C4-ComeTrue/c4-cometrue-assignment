package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.type.SendingType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
	static final long DEFAULT_CHARGE_LIMIT = 3_000_000L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "email", unique = true, nullable = false, length = 50)
	private String email;

	@Column(name = "charge_limit", nullable = false)
	private long chargeLimit;

	@Column(name = "acc_charge", nullable = false)
	private long accCharge;

	@Enumerated(EnumType.STRING)
	@Column(name = "sending_type")
	private SendingType sendingType;

	@Builder
	private User(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt,
		String email, Long chargeLimit, long accCharge, LocalDateTime chargeLimitDate) {
		super(createdAt, updatedAt, deletedAt);
		this.email = email;
		this.chargeLimit = chargeLimit != null ? chargeLimit : DEFAULT_CHARGE_LIMIT;
		this.accCharge = accCharge;
	}
}
