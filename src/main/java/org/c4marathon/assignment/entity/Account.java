package org.c4marathon.assignment.entity;

import java.time.Instant;

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
    private static final int DEFAULT_CHARGE_LIMIT = 3_000_000;

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
    private Integer dailyLimit;

    @Column(name = "daily_charged_amount", nullable = false)
    private Integer dailyChargeAmount ;

    @Column(name = "daily_charged_amount_updated_date", nullable = false)
    private Instant dailyChargeAmountUpdatedDate;

    public Account(Long userId) {
        this.userId = userId;
        this.balance = 0L;
        this.dailyLimit = DEFAULT_CHARGE_LIMIT;
        this.dailyChargeAmount = 0;
        this.dailyChargeAmountUpdatedDate = Instant.now();
    }
}