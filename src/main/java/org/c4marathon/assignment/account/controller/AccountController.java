package org.c4marathon.assignment.account.controller;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.config.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 메인 계좌에 돈 충전
    @PostMapping("/account/charge")
    public ResponseEntity<CommonResponse> chargingMoney(){}

    // 적금 계좌 생성
    @PostMapping("/account/create")
    public ResponseEntity<CommonResponse> CreateSavingsAccount(){}

    // 적금 계좌로 돈 송금
    @PostMapping("/account/send")
    public ResponseEntity<CommonResponse> sendMoney(){}

}
