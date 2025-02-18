package org.c4marathon.assignment.entity;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
	name = "transfer_transaction",
	indexes = {
		@Index(name = "idx_status_type_create_date", columnList = "status, type, createDate")
	}
)
public class TransferTransaction extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transfer_transaction_id", nullable = false)
	private Long id;

	@Size(max = 30)
	@NotNull
	@Column(name = "sender", nullable = false, length = 30)
	private String sender;

	@NotNull
	@Column(name = "sender_main_account", nullable = false)
	private Long senderMainAccount;

	@NotNull
	@Column(name = "receiver_main_account", nullable = false)
	private Long receiverMainAccount;

	@NotNull
	@Column(name = "receiver_id", nullable = false)
	private Long receiverId;

	@NotNull
	@ColumnDefault("0")
	@Column(name = "amount", nullable = false)
	private Long amount;

	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	@Enumerated(EnumType.STRING)
	private TransactionType type;

	@Builder
	public TransferTransaction(String sender, Long senderMainAccount, Long receiverMainAccount, Long amount,
		Long receiverId, TransactionType type) {
		this.sender = sender;
		this.senderMainAccount = senderMainAccount;
		this.receiverMainAccount = receiverMainAccount;
		this.amount = amount;
		this.receiverId = receiverId;
		this.status = TransactionStatus.PENDING;
		this.type = type;
	}
}
