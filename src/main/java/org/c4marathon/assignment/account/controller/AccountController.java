package org.c4marathon.assignment.account.controller;

import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.TransferToSavingAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/recharge")
    public void rechargeAccount(
        @Valid
        @RequestBody
        RechargeAccountRequestDto rechargeAccountRequestDto
    ) {
        accountService.rechargeAccount(rechargeAccountRequestDto);
    }

    @Operation(summary = "메인 계좌에서 적금 계좌로 이체")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/saving")
    public void rechargeAccount(
        @Valid
        @RequestBody
        TransferToSavingAccountRequestDto transferToSavingAccountRequestDto
    ) {
        accountService.transferFromRegularAccount(transferToSavingAccountRequestDto);
    }
}
