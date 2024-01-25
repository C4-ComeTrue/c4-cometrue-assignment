package org.c4marathon.assignment.account.dto;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;

public class ResponseDto {

    public record AccountDto(

        Long id,
        Integer balance,
        Integer dailyLimit,
        Type type
    ) {

        public static AccountDto entityToDto(Account account) {

            return new AccountDto(
                account.getId(),
                account.getBalance(),
                account.getDailyLimit(),
                account.getType()
            );
        }
    }
}
