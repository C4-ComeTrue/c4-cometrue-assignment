package org.c4marathon.assignment.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.c4marathon.assignment.domain.account.entity.SavingAccount;

@Getter
@AllArgsConstructor
public class AccountResponseDto {
    private long accountId;
    private long balance;
    private double interestRate;

    public static AccountResponseDto of(SavingAccount savingAccount){
        return new AccountResponseDto(savingAccount.getId(), savingAccount.getBalance(), savingAccount.getInterestRate());
    }
}
