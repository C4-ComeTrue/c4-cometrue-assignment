package org.c4marathon.assignment.domain.account.entity;

import jakarta.persistence.*;
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

    private long dailyTopUpAmount; // 하루 총 충전 금액, 3_000_000을 넘으면 안 된다.
    // 하루제한을 넘었는지 확인할 때 컬럼에 제약조건을 거는 게 나은가 혹은 트랜잭션 안에서 관리하는게 더 나은가

    public Account(User user){
        this.user = user;
        balance = 0;
        dailyTopUpAmount = 0;
    }

    public void topUp(long amount){
        balance += amount;
        dailyTopUpAmount += amount; // TODO 제한을 넘겼는가
    }

}
