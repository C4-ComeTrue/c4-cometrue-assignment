package org.c4marathon.assignment.api;

import org.c4marathon.assignment.api.dto.CreateAccountDto;
import org.c4marathon.assignment.api.dto.TransferAccountDto;
import org.c4marathon.assignment.service.AccountService;
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
@RequestMapping("/v1/accounts")
public class AccountController {

	private final AccountService accountService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreateAccountDto.Res create(@Valid @RequestBody CreateAccountDto.Req req) {
		return accountService.createAccount(req.memberId(), req.name(), req.accountNumber());
	}

	@PostMapping("/transfer")
	@ResponseStatus(HttpStatus.OK)
	public TransferAccountDto.Res transfer(@Valid @RequestBody TransferAccountDto.Req req) {
		return accountService.transfer(req.accountId(), req.accountNumber(), req.transferAmount());
	}
}
