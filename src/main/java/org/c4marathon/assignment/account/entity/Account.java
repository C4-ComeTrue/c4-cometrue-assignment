package org.c4marathon.assignment.account.entity;

import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.util.entity.BaseEntity;

import jakarta.persistence.Column;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(name = "account", indexes = @Index(name = "idx_account_member_id", columnList = "member_id"))
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Column(name = "daily_limit", nullable = false)
    private Integer dailyLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Account(Type type, Member member) {

        this.balance = 0L;
        this.dailyLimit = 0;
        this.type = type;
        this.member = member;
    }

    public void resetDailyLimit(Integer dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void transferBalance(Long balance) {
        this.balance = balance;
    }
}
