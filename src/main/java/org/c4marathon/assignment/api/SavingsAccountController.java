package org.c4marathon.assignment.api;

import org.c4marathon.assignment.api.dto.ChargeSavingsAccountDto;
import org.c4marathon.assignment.api.dto.CreateSavingsAccountDto;
import org.c4marathon.assignment.service.SavingsAccountService;
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
@RequestMapping("/v1/accounts/savings")
public class SavingsAccountController {

	private final SavingsAccountService savingsAccountService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreateSavingsAccountDto.Res create(@Valid @RequestBody CreateSavingsAccountDto.Req req) {
		return savingsAccountService.createSavingsAccount(req.memberId(), req.name(),
			req.withdrawAmount(), req.savingsType());
	}

	@PostMapping("/charge")
	@ResponseStatus(HttpStatus.OK)
	public ChargeSavingsAccountDto.Res chargeFreeSavings(@Valid @RequestBody ChargeSavingsAccountDto.Req req) {
		return savingsAccountService.transferForFreeSavings(req.accountId(), req.chargeAmount());
	}
}
