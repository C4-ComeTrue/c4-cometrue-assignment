package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.application.TransactionService;
import org.c4marathon.assignment.domain.dto.request.TransferRequest;
import org.c4marathon.assignment.domain.dto.request.WithdrawRequest;
import org.c4marathon.assignment.domain.dto.response.TransferResult;
import org.c4marathon.assignment.domain.dto.response.WithdrawResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {
	private final TransactionService transactionService;

	@PostMapping("/withdraw")
	public ResponseEntity<WithdrawResult> withdraw(@RequestBody WithdrawRequest request) {
		return ResponseEntity.ok(transactionService.withdraw(request.accountNumber(), request.money()));
	}

	@PostMapping("/transfer")
	public ResponseEntity<TransferResult> transfer(@RequestBody TransferRequest request) {
		return ResponseEntity.ok(
			transactionService.transfer(request.senderAccountNumber(), request.receiverAccountNumber(), request.money()));
	}
}
