package org.c4marathon.assignment.domain.account.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.global.common.BaseEntity;
import org.c4marathon.assignment.domain.user.entity.User;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PositiveOrZero
    private long balance; // 계좌의 잔고

    @PositiveOrZero
    @Max(value = 3_000_000, message = "충전 금액은 3,000,000을 초과할 수 없습니다.")
    private long dailyTopUpAmount;

    public Account(User user){
        this.user = user;
        balance = 0;
        dailyTopUpAmount = 0;
    }

    public void deposit(long amount){
        balance += amount;
        dailyTopUpAmount += amount;
    }

    public void withdraw(long amount){
        balance -= amount;
    }

}
