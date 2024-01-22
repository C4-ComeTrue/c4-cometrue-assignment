package org.c4marathon.assignment.bankaccount.limit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 충전 한도 관리
 */
@Component
public class ChargeLimitManager {
	private Map<Long, Integer> chargeLimit = new ConcurrentHashMap<>(); // 사용자별 남은 충전 한도

	public void init(long pk) {
		chargeLimit.put(pk, LimitConst.CHARGE_LIMIT);
	}

	public int get(long pk) {
		return chargeLimit.get(pk);
	}

	public boolean charge(long pk, int money) {
		int totalMoney = chargeLimit.get(pk);

		if (totalMoney >= money) {
			totalMoney -= money;
			chargeLimit.put(pk, totalMoney);
			return true;
		}
		return false;
	}
}
