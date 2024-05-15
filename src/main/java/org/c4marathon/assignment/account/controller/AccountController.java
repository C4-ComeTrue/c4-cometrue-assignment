package org.c4marathon.assignment.account.controller;

import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.TransferToOtherAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "메인 계좌 조회")
    @GetMapping("")
    public ResponseEntity<AccountResponseDto> findAccount(
    ) {
        AccountResponseDto accountResponseDto = accountService.findAccount();
        return ResponseEntity.ok(accountResponseDto);
    }

    @Operation(summary = "메인 계좌 충전")
    @PostMapping("/recharge")
    public ResponseEntity rechargeAccount(
        @Valid
        @RequestBody
        RechargeAccountRequestDto rechargeAccountRequestDto
    ) {
        accountService.rechargeAccount(rechargeAccountRequestDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "메인 계좌에서 적금 계좌로 이체")
    @PostMapping("/transfer/saving")
    public ResponseEntity rechargeAccount(
        @Valid
        @RequestBody
        TransferToOtherAccountRequestDto transferToOtherAccountRequestDto
    ) {
        accountService.transferFromRegularAccount(transferToOtherAccountRequestDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자의 메인 계좌에서 친구의 메인 계좌로 송금")
    @PostMapping("/transfer/regular")
    public ResponseEntity transferToOtherAccount(
        @Valid
        @RequestBody
        TransferToOtherAccountRequestDto transferToOtherAccountRequestDto
    ) {
        accountService.transferToOtherAccount(transferToOtherAccountRequestDto);
        return ResponseEntity.noContent().build();
    }
}
