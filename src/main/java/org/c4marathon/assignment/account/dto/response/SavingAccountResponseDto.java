package org.c4marathon.assignment.account.dto.response;

import org.c4marathon.assignment.account.entity.SavingAccount;
import org.c4marathon.assignment.account.entity.Type;

public record SavingAccountResponseDto(
    Long id,
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
