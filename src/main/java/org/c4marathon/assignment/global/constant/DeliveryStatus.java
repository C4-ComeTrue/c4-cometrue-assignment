package org.c4marathon.assignment.global.constant;

public enum DeliveryStatus {

	BEFORE_DELIVERY,
	IN_DELIVERY,
	COMPLETE_DELIVERY;

	public boolean isPending() {
		return this == BEFORE_DELIVERY;
	}

	public boolean isDelivering() {
		return this == IN_DELIVERY;
	}

	public boolean isDelivered() {
		return this == COMPLETE_DELIVERY;
	}
}
