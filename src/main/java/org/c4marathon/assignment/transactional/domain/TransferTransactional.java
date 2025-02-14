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
public class TransferTransactional {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transactional_id")
	private Long id;

	@Column(nullable = false)
	private Long senderAccountId;

	@Column(nullable = false)
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
	private TransferTransactional(Long senderAccountId, Long receiverAccountId, long amount, TransactionalType type,
		TransactionalStatus status, LocalDateTime sendTime, LocalDateTime receiverTime) {
		this.senderAccountId = senderAccountId;
		this.receiverAccountId = receiverAccountId;
		this.amount = amount;
		this.type = type;
		this.status = status;
		this.sendTime = sendTime;
		this.receiverTime = receiverTime;
	}

	public static TransferTransactional create(Long senderAccountId, Long receiverAccountId, long amount,
		TransactionalType type, TransactionalStatus status, LocalDateTime sendTime) {

		return TransferTransactional.builder()
			.senderAccountId(senderAccountId)
			.receiverAccountId(receiverAccountId)
			.amount(amount)
			.type(type)
			.status(status)
			.sendTime(sendTime)
			.build();
	}

	public void setReceiverTime(LocalDateTime localDateTime) {
		this.receiverTime = localDateTime;
	}

	public void updateStatus(TransactionalStatus status) {
		this.status = status;
	}




}
