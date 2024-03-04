package org.c4marathon.assignment.bankaccount.entity;

import java.time.LocalDateTime;

import org.c4marathon.assignment.common.entity.BaseEntity;
import org.c4marathon.assignment.common.utils.ConstValue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

/**
 * Member Entity와 1:1 연관 관계를 맺으려 했지만 관계의 주인이 아닌 쪽에서 조회를 할 때 지연 로딩이 되지 않는 이슈가 있습니다.
 * 페이 시스템 특성 상 메인 화면에선 회원 정보가 필요하고 이체를 하면 계좌 정보가 필요하여 둘 다 자주 조회될 것으로 생각했습니다.
 * 그래서 불필요한 추가의 쿼리 발생을 없애기 위해 별도의 테이블로 분리하고 애플리케이션 수준에서 관리하고 있습니다.
 */
@Entity
@Getter
public class MainAccount extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_pk", nullable = false, updatable = false)
	private long accountPk;

	@Column(name = "money", nullable = false)
	private long money;

	// 최대 충전 한도
	@Column(name = "charge_money", nullable = false)
	private long chargeLimit;

	// 추가로 충전할 수 있는 금액
	@Column(name = "spare_money", nullable = false)
	private long spareMoney;

	public MainAccount() {
		this.money = 0L;
		this.chargeLimit = ConstValue.LimitConst.CHARGE_LIMIT;
		this.spareMoney = ConstValue.LimitConst.CHARGE_LIMIT;
	}

	public void minusMoney(long money) {
		this.money -= money;
	}

	public void chargeCheck() {
		int lastDay = this.getUpdatedAt().getDayOfMonth();
		LocalDateTime now = LocalDateTime.now();
		int nowDay = now.getDayOfMonth();
		if (lastDay != nowDay) {
			this.setUpdatedAt(now);
			this.chargeLimit = ConstValue.LimitConst.CHARGE_LIMIT;
			this.spareMoney = ConstValue.LimitConst.CHARGE_LIMIT;
		}
	}

	public boolean charge(long money) {
		if (this.spareMoney >= money) {
			this.spareMoney -= money;
			this.money += money;
			return true;
		}
		return false;
	}
}
