package org.c4marathon.assignment.bankaccount.entity;

import org.c4marathon.assignment.common.entity.BaseEntity;
import org.c4marathon.assignment.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 적금 계좌 Entity
 */
@Entity
@NoArgsConstructor
@Getter
@Table(name = "saving_account")
public class SavingAccount extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_pk", nullable = false, updatable = false)
	private long accountPk;

	@Column(name = "saving_money", nullable = false)
	private long savingMoney; // 적금에 넣은 돈

	@Column(name = "rate", nullable = false)
	private long rate; // 정수로 계산하고 소수점 아래는 버림. 3.15 -> (*315 / 10000)을 하면 이자가 나옴

	@Column(name = "product_name", length = 30, nullable = false)
	private String productName; // 상품 명(현재는 정기, 자유)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_pk", nullable = false)
	private Member member;

	public SavingAccount(String productName, int rate) {
		this.savingMoney = 0L;
		this.productName = productName;
		this.rate = rate;
	}

	public void addMember(Member member) {
		this.member = member;
	}

	public void addMoney(long money) {
		this.savingMoney += money;
	}
}
