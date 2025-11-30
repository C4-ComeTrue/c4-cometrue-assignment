package org.c4marathon.assignment.domain.entity;

import org.c4marathon.assignment.domain.TransferStatus;

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

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransferLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long sendAccountId;

	private String receiveAccountNumber;

	private Long amount;

	// status : 만약에 바로 이체가 아닌 중간에 pending 상태가 필요한 경우
	@Enumerated(EnumType.STRING)
	private TransferStatus status;

	@Builder
	public TransferLog(Long sendAccountId, String receiveAccountNumber, Long amount, TransferStatus transferStatus) {
		this.sendAccountId = sendAccountId;
		this.receiveAccountNumber = receiveAccountNumber;
		this.amount = amount;
		this.status = transferStatus;
	}

	public void changeCompleted() {
		this.status = TransferStatus.SUCCESS;
	}
}
