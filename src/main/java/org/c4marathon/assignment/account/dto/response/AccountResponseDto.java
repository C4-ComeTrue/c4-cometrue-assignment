package org.c4marathon.assignment.account.dto.response;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;

import jakarta.validation.constraints.PositiveOrZero;

public record AccountResponseDto(
    Long id,
    @PositiveOrZero
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
