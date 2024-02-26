package org.c4marathon.assignment.account.dto.response;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;

public record AccountResponseDto(
    Long id,
    Long balance,
    Integer dailyLimit,
    Type type
) {

    public static AccountResponseDto entityToDto(Account account) {

        return new AccountResponseDto(
            account.getId(),
            account.getBalance(),
            account.getDailyLimit(),
            account.getType()
        );
    }
}
