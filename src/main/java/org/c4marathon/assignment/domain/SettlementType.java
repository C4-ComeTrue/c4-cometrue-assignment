package org.c4marathon.assignment.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.c4marathon.assignment.global.CommonUtils;

public enum SettlementType {
	/**
	 * 일반 정산은 금액을 전부 나누어 지급한 뒤, 남은 금액에 대해서는 랜덤하게 시작 사용자를 정하고,
	 * 그 시작 사용자부터 순차적으로 1원씩 추가 부과합니다.
	 */
	NORMAL((size, money) -> {
		List<Long> divided = new ArrayList<>();
		IntStream.range(0, size).forEach(i -> divided.add(money / size));

		if (money % size != 0) {
			int startIdx = CommonUtils.getRandom(0, size);

			IntStream.range(0, (int)(money % size)).forEach(next -> divided.set(startIdx + next, divided.get(startIdx + next) + 1));
		}

		return divided;
	}),
	/**
	 * 랜덤의 경우, 남은 금액 내에서 랜덤하게 지불 금액을 선택합니다. 이를 사용자 수만큼 반복합니다.
	 */
	RANDOM((size, money) -> {
		List<Long> divided = new ArrayList<>();

		long curLimit = 0L;
		for (int i = 0; i < size - 1; i++) {
			long preLimit = curLimit;

			curLimit = curLimit + CommonUtils.getRandom(0L, money - curLimit);
			divided.add(curLimit - preLimit);
		}
		divided.add(money - curLimit);

		return divided;
	});

	private BiFunction<Integer, Long, List<Long>> settlementFunc;

	SettlementType(BiFunction<Integer, Long, List<Long>> settlementFunc) {
		this.settlementFunc = settlementFunc;
	}

	public List<Long> settle(int size, long money) {
		return settlementFunc.apply(size, money);
	}
}
