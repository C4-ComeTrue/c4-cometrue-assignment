package org.c4marathon.assignment.bankaccount.controller;

import org.c4marathon.assignment.bankaccount.dto.request.ChargeMoneyRequestDto;
import org.c4marathon.assignment.bankaccount.dto.request.SendMoneyRequestDto;
import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;
import org.c4marathon.assignment.bankaccount.service.MainAccountService;
import org.c4marathon.assignment.common.annotation.Login;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts/main")
public class MainAccountController {

	private final MainAccountService mainAccountService;

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/charge")
	public long chargeMoney(@Login SessionMemberInfo memberInfo,
		@Valid @RequestBody ChargeMoneyRequestDto requestDto) {
		return mainAccountService.chargeMoney(memberInfo.mainAccountPk(), requestDto.money(),
			memberInfo.chargeLimitPk());
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/send/saving")
	public void sendToSavingAccount(@Login SessionMemberInfo memberInfo,
		@Valid @RequestBody SendMoneyRequestDto sendAccountInfo) {
		mainAccountService.sendToSavingAccount(memberInfo.mainAccountPk(), sendAccountInfo.accountPk(),
			sendAccountInfo.money(), memberInfo.chargeLimitPk());
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping
	public MainAccountResponseDto getMainAccountInfo(@Login SessionMemberInfo memberInfo) {
		return mainAccountService.getMainAccountInfo(memberInfo.mainAccountPk());
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/send/other")
	public void sendToOtherAccount(@Login SessionMemberInfo memberInfo,
		@Valid @RequestBody SendMoneyRequestDto sendAccountInfo) {
		mainAccountService.sendToOtherAccount(memberInfo.mainAccountPk(), sendAccountInfo.accountPk(),
			sendAccountInfo.money(), memberInfo.chargeLimitPk());
	}
}
