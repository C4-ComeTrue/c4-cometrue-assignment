package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.application.AccountService;
import org.c4marathon.assignment.domain.type.AccountType;
import org.c4marathon.assignment.domain.dto.response.CreatedAccountInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@PostMapping("/create")
	public ResponseEntity<CreatedAccountInfo> create(@RequestParam long userId, @RequestParam AccountType accountType) {
		return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(userId, accountType));
	}
}
