package org.c4marathon.assignment.bankaccount.entity;

import org.c4marathon.assignment.common.utils.ConstValue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 *
 * 생성 시간, 업데이트 시간은 뺐습니다. 이 데이터로 딱히 뭔가를 할 것 같다고 생각이 들지 않았기 때문입니다.
 * step 1의 코드 리뷰를 통해 캐시를 글로벌로 써야겠다!라는 생각에 레디스를 사용했지만, 00시에 초기화 하는 곳에서 문제가 발생했습니다.
 * 전체 삭제하는 메소드가 O(N)의 시간이 걸려서 RedisTemplate에서 deprecated 되었기 때문입니다.
 * 그러다 특강에서 더 자세한 이유를 듣게 되었고 캐싱하는 집착?을 포기하고 데이터베이스에 관리하기로 결정했습니다.
 *
 * MainAccount와 같은 테이블에서 데이터를 관리하는 방법도 생각해 보았습니다.
 * 하지만 이 방법의 경우 00시에 충전 한도를 일괄 업데이트를 하면 MainAccount의 모든 레코드에 락이 걸릴 것입니다.
 * 페이 서비스 특성 상 은행의 점검 시간 때문에 충전은 안되어도 현재 잔고에서 이체나 구매 등은 자유롭게 됩니다.
 * 그렇기에 별도로 분리해서 ChargeLimit 테이블만 전체 업데이트를 하고 MainAccount에서의 작업은 가능하도록 별도의 테이블로 분리했습니다.
 *
 * Member - MainAccount 처럼 둘 다 조회를 하는 경우가 많기에 Member와 관계를 맺지 않고, chargeLimit의 pk를 Member가 관리하는 방법을 사용했습니다.
 */
@Entity
@Getter
@Table(indexes = {@Index(name = "check_index", columnList = "charge_check")})
public class ChargeLimit {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "limit_pk", nullable = false, updatable = false)
	private long limitPk;

	// 최대 충전 한도
	@Column(name = "limit_money", nullable = false)
	private long limitMoney;

	// 추가로 충전할 수 있는 금액
	@Column(name = "spare_money", nullable = false)
	private long spareMoney;

	// 오늘 충전 여부
	@Column(name = "charge_check", nullable = false)
	private boolean chargeCheck;

	public ChargeLimit() {
		this.limitMoney = ConstValue.LimitConst.CHARGE_LIMIT;
		this.spareMoney = ConstValue.LimitConst.CHARGE_LIMIT;
		this.chargeCheck = false;
	}

	public boolean charge(long money) {
		if (spareMoney >= money) {
			spareMoney -= money;
			this.chargeCheck = true;
			return true;
		}
		return false;
	}
}
