package org.c4marathon.assignment.common.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransferEvent extends ApplicationEvent {

	private final Long sendAccountId;
	private final String receiveAccountNumber;
	private final Long amount;

	public TransferEvent(Object source, Long sendAccountId, String receiveAccountNumber, Long amount) {
		super(source);
		this.sendAccountId = sendAccountId;
		this.receiveAccountNumber = receiveAccountNumber;
		this.amount = amount;
	}
}
