package org.c4marathon.assignment.account.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.c4marathon.assignment.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SavingAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saving_account_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String savingAccountNumber;

    @Column(nullable = false)
    private long balance;

    @Column(nullable = false)
    private long depositAmount; //가입 금액

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_product_id")
    private SavingProduct savingProduct;

    // 정기적금일 때 연동된 나의 메인계좌번호
    private String fixedAccountNumber;

    @Builder
    private SavingAccount(long balance, SavingProduct savingProduct, long depositAmount, String savingAccountNumber,
        String fixedAccountNumber) {

        this.balance = balance;
        this.savingProduct = savingProduct;
        this.depositAmount = depositAmount;
        this.savingAccountNumber = savingAccountNumber;
        this.fixedAccountNumber = fixedAccountNumber;
    }

    public static SavingAccount create(long balance, SavingProduct savingProduct,long depositAmount,
        String savingAccountNumber, String fixedAccountNumber) {

        if (savingProduct.getType().equals(SavingProductType.FREE)) {
            return SavingAccount.builder()
                .balance(balance)
                .savingProduct(savingProduct)
                .depositAmount(depositAmount)
                .savingAccountNumber(savingAccountNumber)
                .build();
        }
        return SavingAccount.builder()
            .savingAccountNumber(savingAccountNumber)
            .balance(balance)
            .savingProduct(savingProduct)
            .depositAmount(depositAmount)
            .fixedAccountNumber(fixedAccountNumber)
            .build();
    }

    public void deposit(long money) {
        this.balance += money;
    }
}
