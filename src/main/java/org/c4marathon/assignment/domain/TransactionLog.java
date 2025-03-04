package org.c4marathon.assignment.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;

@Getter
@Document(collection = "transaction_log")
public class TransactionLog {
	@Id
	private String id;

	@Field(name = "transaction_id")
	private Long transactionId;

	@Field(name = "sender_id")
	private Long senderId;

	@Field(name = "sender_name")
	private String senderName;

	@Field(name = "sending_name")
	private String sendingName;

	@Field(name = "sender_account_number")
	private String senderAccountNumber;

	@Field(name = "sender_email")
	private String senderEmail;

	@Field(name = "receiver_id")
	private Long receiverId;

	@Field(name = "receiver_name")
	private String receiverName;

	@Field(name = "receiver_email")
	private String receiverEmail;

	@Field(name = "receiver_account_number")
	private String receiverAccountNumber;

	@Field(name = "money")
	private long money;

	@Field(name = "send_time")
	private Instant sendTime;

	@Builder
	protected TransactionLog(Long transactionId, Long senderId, String senderName, String sendingName, String senderAccountNumber,
		String senderEmail, Long receiverId, String receiverName, String receiverEmail, String receiverAccountNumber,
		long money, Instant sendTime) {
		this.transactionId = transactionId;
		this.senderId = senderId;
		this.senderName = senderName;
		this.sendingName = sendingName;
		this.senderAccountNumber = senderAccountNumber;
		this.senderEmail = senderEmail;
		this.receiverId = receiverId;
		this.receiverName = receiverName;
		this.receiverEmail = receiverEmail;
		this.receiverAccountNumber = receiverAccountNumber;
		this.money = money;
		this.sendTime = sendTime;
	}
}
