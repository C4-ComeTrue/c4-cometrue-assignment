package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.dto.request.PostMainAccountReq;
import org.c4marathon.assignment.dto.request.PostSavingsAccountReq;
import org.c4marathon.assignment.dto.request.TransferReq;
import org.c4marathon.assignment.dto.request.WithdrawMainAccountReq;
import org.c4marathon.assignment.dto.response.MainAccountInfoRes;
import org.c4marathon.assignment.dto.response.TransferRes;
import org.c4marathon.assignment.dto.response.WithdrawInfoRes;
import org.c4marathon.assignment.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/account")
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@PostMapping("/savings")
	public ResponseEntity<Void> createSavingsAccount(@RequestBody @Valid PostSavingsAccountReq postSavingsAccountReq) {
		accountService.createSavingsAccount(postSavingsAccountReq);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	@PostMapping("/main/deposit")
	public MainAccountInfoRes depositMainAccount(@RequestBody @Valid PostMainAccountReq postMainAccountReq) {
		return accountService.depositMainAccount(postMainAccountReq);
	}
	@PostMapping("/savings/withdraw")
	public WithdrawInfoRes withdrawForSavings(@RequestBody @Valid WithdrawMainAccountReq withdrawMainAccountReq) {
		return accountService.withdrawForSavings(withdrawMainAccountReq);
	}

	@PostMapping("/main/transfer")
	public TransferRes transfer(@RequestBody @Valid TransferReq transferReq) {
		return accountService.transfer(transferReq);
	}
}
