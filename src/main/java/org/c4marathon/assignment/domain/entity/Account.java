package org.c4marathon.assignment.domain.entity;

import java.time.LocalDate;
import java.time.ZoneId;

import org.c4marathon.assignment.common.utils.ChargeLimitUtils;

import jakarta.persistence.Entity;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)   // TEST
@Table(
	indexes = {@Index(name = "account_member_index", columnList = "member_id")}
)
public class Account extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private String name;

	private String accountNumber;

	@NotNull
	@PositiveOrZero
	private long amount;

	@NotNull
	@PositiveOrZero
	private long accumulatedChargeAmount;     // 사용자가 하루 동안 누적한 충전 금액

	@NotNull
	@PositiveOrZero
	private long chargeLimit = ChargeLimitUtils.BASIC_LIMIT;     // 충전 한도

	@NotNull
	private LocalDate chargeUpdatedAt = LocalDate.now(ZoneId.of("Asia/Seoul"));

	@Builder
	public Account(Member member, String name, String accountNumber) {
		this.member = member;
		this.name = name;
		this.accountNumber = accountNumber;
	}

	public void charge(long amount) {
		this.amount += amount;
		this.accumulatedChargeAmount += amount;
	}

	public void withdraw(long amount) {
		this.amount -= amount;
	}

	public void initializeChargeAmount() {
		this.accumulatedChargeAmount = 0;
		this.chargeUpdatedAt = LocalDate.now(ZoneId.of("Asia/Seoul"));
	}
}
