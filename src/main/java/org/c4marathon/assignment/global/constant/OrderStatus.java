package org.c4marathon.assignment.global.constant;

public enum OrderStatus {

	COMPLETE_PAYMENT,
	CONFIRM,
	REFUND;

	public boolean isPayedUp() {
		return this == COMPLETE_PAYMENT;
	}
}
