package org.c4marathon.assignment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SavingsType {

	REGULAR(5),   // 정기 적금 :이율 5%
	FREE(3);      // 자유 적금 : 이율 3%

	private final double percent;

	public void calculateInterest() {
		// TODO
	}
}
