package org.c4marathon.assignment.common.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransferEvent extends ApplicationEvent {

	private final Long transferId;

	private final Long sendAccountId;
	private final String receiveAccountNumber;
	private final Long amount;

	public TransferEvent(Object source, Long transferId, Long sendAccountId, String receiveAccountNumber, Long amount) {
		super(source);
		this.transferId = transferId;
		this.sendAccountId = sendAccountId;
		this.receiveAccountNumber = receiveAccountNumber;
		this.amount = amount;
	}
}
