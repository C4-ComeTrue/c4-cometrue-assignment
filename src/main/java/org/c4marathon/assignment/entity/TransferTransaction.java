package org.c4marathon.assignment.entity;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "transfer_transaction")
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
	@ColumnDefault("0")
	@Column(name = "amount", nullable = false)
	private Long amount;

	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	@Builder
	public TransferTransaction(String sender, Long senderMainAccount, Long receiverMainAccount, Long amount) {
		this.sender = sender;
		this.senderMainAccount = senderMainAccount;
		this.receiverMainAccount = receiverMainAccount;
		this.amount = amount;
		this.status = TransactionStatus.PENDING;
	}
}
