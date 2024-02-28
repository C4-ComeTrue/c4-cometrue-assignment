package org.c4marathon.assignment.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
	indexes = {@Index(name = "charge_link_account_index", columnList = "account_id")}
)
public class ChargeLinkedAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account account;

	@NotBlank
	private String bank;

	@NotBlank
	private String accountNumber;

	@NotNull
	private boolean main;       // 주 연동 계좌 여부

	@PositiveOrZero
	private long amount;        // 임시 잔액 TODO: 외부 계좌 조회 API 연동

	@Builder
	public ChargeLinkedAccount(Account account, String bank, String accountNumber, boolean main, long amount) {
		this.account = account;
		this.bank = bank;
		this.accountNumber = accountNumber;
		this.main = main;
		this.amount = amount;
	}

	public void withdraw(long amount) {
		this.amount -= amount;
	}

	public boolean isAmountLackToWithDraw(long withDrawAmount) {
		return this.amount < withDrawAmount;
	}
}
