package org.c4marathon.assignment.domain;

public enum SavingsType {

	REGULAR(5),   // 정기 적금 :이율 5%
	FREE(3);      // 자유 적금 : 이율 3%

	private final double percent;

	SavingsType(double percent) {
		this.percent = percent;
	}

	public void calculateInterest() {
		// TODO
	}
}


