package org.c4marathon.assignment.account.controller;

import java.util.List;

import org.c4marathon.assignment.account.dto.request.AccountRequestDto;
import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.SavingAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @Operation(summary = "추가 계좌 생성")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("")
    public void createAccount(
        @Valid
        @RequestBody
        AccountRequestDto accountRequestDto,
        @RequestHeader(value = "Authorization")
        String accessToken
    ) {

        accountService.saveAccount(accountRequestDto, accessToken);
    }

    @Operation(summary = "계좌 전체 조회")
    @GetMapping("")
    public ResponseEntity<List<AccountResponseDto>> findAccount(
        @RequestHeader(value = "Authorization")
        String accessToken
    ) {

        List<AccountResponseDto> accountResponseDtoList = accountService.findAccount(accessToken);
        return ResponseEntity.ok(accountResponseDtoList);
    }

    @Operation(summary = "메인 계좌 충전")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/recharge")
    public void rechargeAccount(
        @Valid
        @RequestBody
        RechargeAccountRequestDto rechargeAccountRequestDto,
        @RequestHeader(value = "Authorization")
        String accessToken
    ) {
        accountService.rechargeAccount(rechargeAccountRequestDto, accessToken);
    }

    @Operation(summary = "메인 계좌에서 적금 계좌로 이체")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/saving")
    public void rechargeAccount(
        @Valid
        @RequestBody
        SavingAccountRequestDto savingAccountRequestDto,
        @RequestHeader(value = "Authorization")
        String accessToken
    ) {
        accountService.transferFromRegularAccount(savingAccountRequestDto, accessToken);
    }
}
