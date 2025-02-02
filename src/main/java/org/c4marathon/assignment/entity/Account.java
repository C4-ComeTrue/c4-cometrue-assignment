package org.c4marathon.assignment.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "account")
public class Account extends BaseEntity {
    private static final long DEFAULT_CHARGE_LIMIT = 3_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "balance", nullable = false)
    private Long balance;

    @Column(name = "daily_limit", nullable = false)
    private Long dailyLimit;

    @Column(name = "daily_charged_amount", nullable = false)
    private Long dailyChargeAmount ;

    @Column(name = "daily_charged_amount_updated_date", nullable = false)
    private Instant dailyChargeAmountUpdatedDate;

    public Account(Long userId) {
        this.userId = userId;
        this.balance = 0L;
        this.dailyLimit = DEFAULT_CHARGE_LIMIT;
        this.dailyChargeAmount = 0L;
        this.dailyChargeAmountUpdatedDate = Instant.now();
    }

    public boolean isDailyLimitExceeded(long amount) {
        LocalDate updatedDate = this.dailyChargeAmountUpdatedDate.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();

        if(updatedDate.isBefore(now)) {
            this.dailyChargeAmount = 0L;
        }

        return this.dailyLimit < dailyChargeAmount + amount ? true : false;
    }

    public void deposit(long amount) {
        this.dailyChargeAmount += amount;
        this.balance += amount;
    }

    public boolean isBalanceInsufficient(long amount) {
        return this.balance < amount ? true : false;
    }

    public void withdraw(long amount) {
        this.balance -= amount;
    }
}