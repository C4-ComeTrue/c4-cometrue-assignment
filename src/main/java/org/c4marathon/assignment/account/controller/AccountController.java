package org.c4marathon.assignment.account.controller;

import java.util.List;

import org.c4marathon.assignment.account.dto.RequestDto;
import org.c4marathon.assignment.account.dto.ResponseDto;
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
        RequestDto.AccountDto accountDto,
        @RequestHeader(value = "Authorization") String accessToken
    ) {

        accountService.saveAccount(accountDto, accessToken);
    }

    @Operation(summary = "계좌 전체 조회")
    @GetMapping("")
    public ResponseEntity<List<ResponseDto.AccountDto>> findAccount(
        @RequestHeader(value = "Authorization") String accessToken
    ) {

        List<ResponseDto.AccountDto> accountDtoList = accountService.findAccount(accessToken);
        return ResponseEntity.ok(accountDtoList);
    }

    @Operation(summary = "메인 계좌 충전")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/recharge")
    public void rechargeAccount(
        @Valid
        @RequestBody
        RequestDto.RechargeAccountDto rechargeAccountDto,
        @RequestHeader(value = "Authorization") String accessToken
    ) {
        accountService.rechargeAccount(rechargeAccountDto, accessToken);
    }

    @Operation(summary = "메인 계좌에서 적금 계좌로 이체")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/saving")
    public void rechargeAccount(
        @Valid
        @RequestBody
        RequestDto.SavingAccountDto savingAccountDto,
        @RequestHeader(value = "Authorization") String accessToken
    ) {
        accountService.transferFromRegularAccount(savingAccountDto, accessToken);
    }
}
