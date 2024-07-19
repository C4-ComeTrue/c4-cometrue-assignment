package org.c4marathon.assignment.domain.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.account.dto.AccountResponseDto;
import org.c4marathon.assignment.domain.account.dto.DepositDto;
import org.c4marathon.assignment.domain.account.dto.TransferDto;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.c4marathon.assignment.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    // 일반 계좌 생성
    @PostMapping("/checking")
    public ResponseEntity createAccount(@AuthenticationPrincipal CustomUserDetails userDetails){
        accountService.createAccount(userDetails.getUsername());
        return new ResponseEntity(HttpStatus.CREATED);
    }

    // 적금 계좌 생성
    @PostMapping("/saving")
    public ResponseEntity createSavingAccount(@AuthenticationPrincipal CustomUserDetails userDetails){
        accountService.createSavingAccount(userDetails.getUsername());
        return new ResponseEntity(HttpStatus.CREATED);
    }

    // 적금 계좌 조회
    @GetMapping("/saving")
    public List<AccountResponseDto> getAllSavingAccount(@AuthenticationPrincipal CustomUserDetails userDetails){
        return accountService.getSavingAccounts(userDetails.getUsername());
    }

    // 메인 계좌에 충전
    @PostMapping("/deposit")
    public DepositDto.Res depositToMainAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody @Valid DepositDto.Req req){
        return accountService.depositToMainAccount(userDetails.getUsername(), req.amount());
    }

    // 메인 계좌 -> 적금 계좌 이체
    @PostMapping("/transfer")
    public TransferDto.Res transferToSavings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody @Valid TransferDto.Req req){
        return accountService.transferToSavings(userDetails.getUsername(), req.savingAccountId(), req.amount());
    }


}
