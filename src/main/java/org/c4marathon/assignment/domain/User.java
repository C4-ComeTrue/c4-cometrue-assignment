package org.c4marathon.assignment.domain;

import java.time.Instant;

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

	@Column(name = "name", nullable = false, length = 50)
	private String name;

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
	private User(Instant createdAt, Instant updatedAt, Instant deletedAt, String name,
		String email, Long chargeLimit, long accCharge, SendingType sendingType) {
		super(createdAt, updatedAt, deletedAt);
		this.name = name;
		this.email = email;
		this.chargeLimit = (chargeLimit != null) ? chargeLimit : DEFAULT_CHARGE_LIMIT;
		this.accCharge = accCharge;
		this.sendingType = (sendingType != null) ? sendingType : SendingType.EAGER;
	}
}
