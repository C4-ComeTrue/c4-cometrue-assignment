package org.c4marathon.assignment.account.controller;

import org.c4marathon.assignment.account.dto.RequestDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.springframework.http.HttpStatus;
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
}
