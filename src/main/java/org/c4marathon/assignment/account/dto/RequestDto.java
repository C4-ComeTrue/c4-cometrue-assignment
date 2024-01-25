package org.c4marathon.assignment.account.dto;

import org.c4marathon.assignment.account.entity.Type;

import jakarta.validation.constraints.NotBlank;

public class RequestDto {

    // 계좌 생성 요청
    public record AccountDto(

        @NotBlank
        Type type
    ) {
    }

    // 메인 계좌 잔액 충전
    public record RechargeAccountDto(

        @NotBlank
        Long accountId,
        @NotBlank
        Integer balance
    ) {
    }
}
