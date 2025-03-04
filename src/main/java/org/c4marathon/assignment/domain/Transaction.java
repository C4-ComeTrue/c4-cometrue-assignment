package org.c4marathon.assignment.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.c4marathon.assignment.domain.type.TransactionState;

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
@Table(name = "transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {
	private static final int DEADLINE_HOURS = 72;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "sending_name", nullable = false, length = 20)
	private String sendingName;

	@Column(name = "sender_account_number", nullable = false, length = 50)
	private String senderAccountNumber;

	@Column(name = "receiver_account_number", nullable = false, length = 50)
	private String receiverAccountNumber;

	@Column(name = "balance", nullable = false)
	private Long balance;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "deadline", nullable = false)
	private Instant deadline;

	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false, length = 20)
	private TransactionState state;

	@Builder
	public Transaction(String sendingName, String senderAccountNumber, String receiverAccountNumber,
		Long balance, Instant createdAt, TransactionState state) {
		this.sendingName = sendingName;
		this.senderAccountNumber = senderAccountNumber;
		this.receiverAccountNumber = receiverAccountNumber;
		this.balance = balance;
		this.createdAt = (createdAt == null) ? Instant.now() : createdAt;
		this.deadline = this.createdAt.plus(DEADLINE_HOURS, ChronoUnit.HOURS);
		this.state = state;
	}
}
