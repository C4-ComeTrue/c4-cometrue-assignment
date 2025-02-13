package org.c4marathon.assignment.transactional.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transactional {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transactional_id")
	private Long id;

	@Column(nullable = false)
	private Long senderAccountId;

	private Long receiverAccountId;

	@Column(nullable = false)
	private long amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionalType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionalStatus status;

	@Column(nullable = false)
	private LocalDateTime sendTime;

	private LocalDateTime receiverTime;

	@Builder
	private Transactional(Long senderAccountId, Long receiverAccountId, long amount, TransactionalType type,
		TransactionalStatus status, LocalDateTime sendTime, LocalDateTime receiverTime) {
		this.senderAccountId = senderAccountId;
		this.receiverAccountId = receiverAccountId;
		this.amount = amount;
		this.type = type;
		this.status = status;
		this.sendTime = sendTime;
		this.receiverTime = receiverTime;
	}

	public static Transactional create(Long senderAccountId, long amount, TransactionalType type,
		TransactionalStatus status, LocalDateTime sendTime) {

		return Transactional.builder()
			.senderAccountId(senderAccountId)
			.amount(amount)
			.type(type)
			.status(status)
			.sendTime(sendTime)
			.build();
	}



}
