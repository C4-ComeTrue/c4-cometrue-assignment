package org.c4marathon.assignment.account.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.exception.UncheckedException;
import org.c4marathon.assignment.user.domain.User;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Getter
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(name = "accountNumber", nullable = false)
    private Long accountNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType type;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "accountPw", nullable = false)
    private int accountPw;

    @Column(name = "limitaccount", nullable = false)
    private int limitaccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private User user;

    public void setAmount(int updatedAmount) {
        this.amount += updatedAmount;
        this.limitaccount -= updatedAmount;
        if(this.limitaccount < 0){
            throw new NullPointerException();
        }
    }

    public void plusAmount(int updatedAmount) {
        this.amount += updatedAmount;
    }

    public void minusAmount(int updatedAmount) {
        this.amount -= updatedAmount;
    }
}
