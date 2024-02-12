package org.c4marathon.assignment.domain.entity;

import java.math.BigInteger;

import org.c4marathon.assignment.domain.ChargeLimit;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)   // TEST
public class Account extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	private String name;

	@NotNull
	private String accountNumber;

	@NotNull
	private BigInteger amount = BigInteger.valueOf(0);     // 현재 보유하고 있는 잔고의 양

	@NotNull
	private int accumulatedChargeAmount;            // 사용자가 1일 동안 누적한 충전 금액 -> 하루 주기로 초기화

	@Enumerated(EnumType.STRING)
	private ChargeLimit chargeLimit = ChargeLimit.DAY_BASIC_LIMIT;     // 인당 한도 설정

	@Builder
	public Account(Member member, String name, String accountNumber) {
		this.member = member;
		this.name = name;
		this.accountNumber = accountNumber;
	}

	public void charge(int amount) {
		this.amount = this.amount.add(BigInteger.valueOf(amount));
		this.accumulatedChargeAmount += amount;
	}
}
