package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.application.TransactionService;
import org.c4marathon.assignment.domain.dto.request.TransactionCancelRequest;
import org.c4marathon.assignment.domain.dto.request.TransactionReceiveRequest;
import org.c4marathon.assignment.domain.dto.request.TransferRequest;
import org.c4marathon.assignment.domain.dto.request.WithdrawRequest;
import org.c4marathon.assignment.domain.dto.response.TransferResult;
import org.c4marathon.assignment.domain.dto.response.WithdrawResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
	public ResponseEntity<TransferResult> wireTransfer(@RequestBody TransferRequest request) {
		return ResponseEntity.ok(
			transactionService.wireTransfer(request.senderAccountNumber(), request.receiverAccountNumber(), request.money()));
	}

	@PostMapping("/receive")
	public ResponseEntity<TransferResult> receive(@RequestBody TransactionReceiveRequest request) {
		return ResponseEntity.ok(transactionService.receive(request.transactionId()));
	}

	@DeleteMapping("/cancel")
	public ResponseEntity<Void> cancel(@RequestBody TransactionCancelRequest request) {
		transactionService.cancel(request.transactionId());

		return ResponseEntity.noContent().build();
	}
}
