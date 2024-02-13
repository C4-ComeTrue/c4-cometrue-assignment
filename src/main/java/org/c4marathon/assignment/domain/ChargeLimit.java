package org.c4marathon.assignment.domain;

import lombok.Getter;


@Getter
public enum ChargeLimit {

	NO_LIMIT(null, 0),
	DAY_BASIC_LIMIT("DAY", 3000000),
	DAY_MAX_LIMIT("DAY", 300000000);

	private final String duration;   // DAY / WEEK
	private final int limitAmount;

	ChargeLimit(String duration, Integer limitAmount) {
		this.duration = duration;
		this.limitAmount = limitAmount;
	}

	public boolean doesExceed(int totalChargeAmount, int chargeAmount) {
		return limitAmount < totalChargeAmount + chargeAmount;
	}
}
