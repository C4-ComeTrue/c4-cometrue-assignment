package org.c4marathon.assignment.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.config.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 메인 계좌에 돈 충전
    @PostMapping("/account/charge")
    public ResponseEntity<CommonResponse> chargingMoney(@Valid @RequestBody ChargeDto chargeDto){

        boolean checkCharge = accountService.chargeMainAccount(chargeDto);

        if (checkCharge == true){
            CommonResponse res = new CommonResponse(
                    200,
                    HttpStatus.OK,
                    "충전 성공",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }else {
            CommonResponse res = new CommonResponse(
                    400,
                    HttpStatus.BAD_REQUEST,
                    "충전 실패",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }
    }

    // 적금 계좌 생성
    @PostMapping("/account/create/{userId}")
    public ResponseEntity<CommonResponse> CreateSavingsAccount(@PathVariable @RequestBody Long userId ){
        boolean checkSaving = accountService.craeteSavingAccount(userId);

        if(checkSaving == true){
            CommonResponse res = new CommonResponse(
                    200,
                    HttpStatus.OK,
                    "적금계좌 생성완료",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }else {
            CommonResponse res = new CommonResponse(
                    400,
                    HttpStatus.BAD_REQUEST,
                    "적금계좌 생성실패",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }
    }

    // 적금 계좌로 돈 송금
    @PostMapping("/account/send/{userId}")
    public ResponseEntity<CommonResponse> sendMoney(@PathVariable @RequestBody Long userId, @Valid @RequestBody SendDto sendDto){
        boolean checkSend = accountService.sendSavingAccount(userId, sendDto);

        if(checkSend == true){
            CommonResponse res = new CommonResponse(
                    200,
                    HttpStatus.OK,
                    "송금완료",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }else {
            CommonResponse res = new CommonResponse(
                    400,
                    HttpStatus.BAD_REQUEST,
                    "송금실패",
                    null
            );
            return new ResponseEntity<>(res, res.getHttpStatus());
        }
    }

}
