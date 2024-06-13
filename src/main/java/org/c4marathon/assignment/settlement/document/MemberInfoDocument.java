package org.c4marathon.assignment.settlement.document;

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
}
