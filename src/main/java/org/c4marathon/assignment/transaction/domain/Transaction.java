package org.c4marathon.assignment.transaction.domain;

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
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transactional_id")
	private Long id;

	@Column(nullable = false)
	private String senderAccountNumber;

	@Column(nullable = false)
	private String receiverAccountNumber;

	@Column(nullable = false)
	private long amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionStatus status;

	@Column(nullable = false)
	private LocalDateTime sendTime;

	private LocalDateTime receiverTime;

	@Builder
	private Transaction(String senderAccountNumber, String receiverAccountNumber, long amount, TransactionType type,
		TransactionStatus status, LocalDateTime sendTime, LocalDateTime receiverTime) {
		this.senderAccountNumber = senderAccountNumber;
		this.receiverAccountNumber= receiverAccountNumber;
		this.amount = amount;
		this.type = type;
		this.status = status;
		this.sendTime = sendTime;
		this.receiverTime = receiverTime;
	}

	public static Transaction create(String senderAccountNumber, String receiverAccountNumber, long amount,
		TransactionType type, TransactionStatus status, LocalDateTime sendTime) {

		return Transaction.builder()
			.senderAccountNumber(senderAccountNumber)
			.receiverAccountNumber(receiverAccountNumber)
			.amount(amount)
			.type(type)
			.status(status)
			.sendTime(sendTime)
			.build();
	}

	public void setReceiverTime(LocalDateTime localDateTime) {
		this.receiverTime = localDateTime;
	}

	public void updateStatus(TransactionStatus status) {
		this.status = status;
	}




}
