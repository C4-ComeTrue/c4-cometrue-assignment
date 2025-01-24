package org.c4marathon.assignment.account.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.c4marathon.assignment.global.entity.BaseEntity;
import org.c4marathon.assignment.global.util.Const;

/**
 * 메인 계좌
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = false)
    private long money;

    @Column(nullable = false)
    private long chargeLimit;

    @Builder
    private Account(long money, long chargeLimit) {
        this.money = money;
        this.chargeLimit = chargeLimit;
    }

    public static Account create() {
        return Account.builder()
                .chargeLimit(Const.CHARGE_LIMIT)
                .build();
    }


    public void chargeAccount(long money) {
        this.money += money;
    }

    public boolean isCharge(long money) {
        if (this.chargeLimit < money) {
            return false;
        }
        this.chargeLimit -= money;
        return true;
    }


}
