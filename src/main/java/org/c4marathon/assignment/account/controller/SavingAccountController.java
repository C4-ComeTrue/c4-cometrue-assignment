package org.c4marathon.assignment.account.controller;

import java.util.List;

import org.c4marathon.assignment.account.dto.request.AccountRequestDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.service.SavingAccountService;
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
@RequestMapping("/accounts/saving")
public class SavingAccountController {

    private final SavingAccountService savingAccountService;

    @Operation(summary = "적금 계좌 생성")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("")
    public void createAccount(
        @Valid
        @RequestBody
        AccountRequestDto accountRequestDto
    ) {
        savingAccountService.saveSavingAccount(accountRequestDto);
    }

    @Operation(summary = "적금 계좌 조회")
    @GetMapping("")
    public ResponseEntity<List<SavingAccountResponseDto>> findSavingAccount(
    ) {
        List<SavingAccountResponseDto> savingAccountResponseDtoList = savingAccountService.findSavingAccount();
        return ResponseEntity.ok(savingAccountResponseDtoList);
    }
}
