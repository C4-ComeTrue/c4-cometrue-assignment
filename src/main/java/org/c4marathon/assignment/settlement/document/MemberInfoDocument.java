package org.c4marathon.assignment.settlement.document;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class MemberInfoDocument {
	private long accountPk;
	private String memberName;
	private long settleMoney;

	public MemberInfoDocument(long accountPk, String memberName, long settleMoney) {
		this.accountPk = accountPk;
		this.memberName = memberName;
		this.settleMoney = settleMoney;
	}

	public void plusLeftMoney() {
		this.settleMoney++;
	}

	public void plusRandomMoney(long randomMoney) {
		this.settleMoney += randomMoney;
	}
}
