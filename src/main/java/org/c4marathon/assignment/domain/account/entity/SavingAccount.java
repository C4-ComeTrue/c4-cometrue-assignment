package org.c4marathon.assignment.domain.saving.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.global.common.BaseEntity;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Saving extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "saving_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private long balance; // 계좌의 잔고

    private double interestRate; // 이자율

    public void updateBalance(long amount){
        balance += amount;
    }
}
