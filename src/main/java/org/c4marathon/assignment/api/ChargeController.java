package org.c4marathon.assignment.api;

import org.c4marathon.assignment.api.dto.ChargeAccountDto;
import org.c4marathon.assignment.api.dto.CreateChargeLinkedAccountDto;
import org.c4marathon.assignment.service.ChargeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/charge-accounts")
public class ChargeController {

	private final ChargeService chargeService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void registerChargeLinkedAccount(@Valid @RequestBody CreateChargeLinkedAccountDto.Req req) {
		chargeService.registerChargeAccount(req.accountId(), req.bank(), req.accountNumber(), req.isMain());
	}

	@PostMapping("/charge")
	@ResponseStatus(HttpStatus.OK)
	public ChargeAccountDto.Res charge(@Valid @RequestBody ChargeAccountDto.Req req) {
		return chargeService.charge(req.accountId(), req.amount());
	}
}
