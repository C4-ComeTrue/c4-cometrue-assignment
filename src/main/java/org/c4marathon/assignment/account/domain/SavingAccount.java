package org.c4marathon.assignment.account.domain;


import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SavingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saving_account_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String savingAccountNumber;

    @Column(nullable = false)
    private long balance; // 현재 적금 금액

    @Column(nullable = false)
    private long depositAmount; // (정기 적금) 매일 자동 입금할 금액

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_product_id")
    private SavingProduct savingProduct;

    @Column(nullable = false)
    private String mainAccountNumber; // 이자 입금 해주는 메인 계좌

    @Column(nullable = false)
    private LocalDateTime startDate; // 개설 날짜

    @Column(nullable = false)
    private LocalDateTime maturityDate; // 만기일

    private boolean isMatured; // 만기 여부

    @Builder
    private SavingAccount(String savingAccountNumber, long balance, long depositAmount, SavingProduct savingProduct,
        String mainAccountNumber, LocalDateTime startDate, LocalDateTime maturityDate) {
        this.savingAccountNumber = savingAccountNumber;
        this.balance = balance;
        this.depositAmount = depositAmount;
        this.savingProduct = savingProduct;
        this.mainAccountNumber = mainAccountNumber;
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.isMatured = false;
    }

    public static SavingAccount create(String savingAccountNumber, long balance, long depositAmount,
        SavingProduct savingProduct, String mainAccountNumber) {

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime maturityDate = startDate.plusMonths(savingProduct.getTermMonths());

        return SavingAccount.builder()
            .savingAccountNumber(savingAccountNumber)
            .balance(balance)
            .depositAmount(depositAmount)
            .savingProduct(savingProduct)
            .mainAccountNumber(mainAccountNumber)
            .startDate(startDate)
            .maturityDate(maturityDate)
            .build();
    }

    public long calculateInterest() {
        double interestRate = savingProduct.getRate() / 100.0;
        return (long) (balance * interestRate / 365);
    }

    public void deposit(long money) {
        this.balance += money;
    }
}
