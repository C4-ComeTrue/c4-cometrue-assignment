package org.c4marathon.assignment.account.dto.response;

import org.c4marathon.assignment.account.entity.SavingAccount;
import org.c4marathon.assignment.account.entity.Type;

import jakarta.validation.constraints.PositiveOrZero;

public record SavingAccountResponseDto(
    Long id,
    @PositiveOrZero
    Long balance,
    Type type
) {

    public static SavingAccountResponseDto entityToDto(SavingAccount savingAccount) {
        return new SavingAccountResponseDto(
            savingAccount.getId(),
            savingAccount.getBalance(),
            savingAccount.getType()
        );
    }
}
