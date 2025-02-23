package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

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

	@Column(name = "sender_account_number", nullable = false, length = 50)
	private String senderAccountNumber;

	@Column(name = "receiver_account_number", nullable = false, length = 50)
	private String receiverAccountNumber;

	@Column(name = "balance", nullable = false)
	private Long balance;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "deadline", nullable = false)
	private LocalDateTime deadline;

	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false, length = 20)
	private TransactionState state;

	@Builder
	public Transaction(String senderAccountNumber, String receiverAccountNumber, Long balance, LocalDateTime createdAt,
		TransactionState state) {
		LocalDateTime now = LocalDateTime.now();

		this.senderAccountNumber = senderAccountNumber;
		this.receiverAccountNumber = receiverAccountNumber;
		this.balance = balance;
		this.createdAt = (createdAt == null) ? now : createdAt;
		this.deadline = this.createdAt.plusHours(DEADLINE_HOURS);
		this.state = state;
	}
}
