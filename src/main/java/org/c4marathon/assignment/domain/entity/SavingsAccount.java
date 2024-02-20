package org.c4marathon.assignment.domain.entity;

import org.c4marathon.assignment.domain.SavingsType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	indexes = {@Index(name = "savings_account_member_index", columnList = "member_id")}
)
public class SavingsAccount extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@NotNull
	@PositiveOrZero
	private long amount;                // 현재 보유하고 있는 잔고의 양

	@Enumerated(EnumType.STRING)
	private SavingsType savingsType;    // 들고 있는 적금 종류

	@PositiveOrZero
	private long withdrawAmount;        // 고정된 출금액

	@Builder
	public SavingsAccount(Member member, String name, SavingsType savingsType, long withdrawAmount) {
		this.member = member;
		this.name = name;
		this.savingsType = savingsType;
		this.withdrawAmount = withdrawAmount;
	}

	public void charge(long amount) {
		this.amount += amount;
	}
}
