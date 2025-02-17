package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.dto.request.TransferAcceptReq;
import org.c4marathon.assignment.dto.response.TransferRes;
import org.c4marathon.assignment.service.TransactionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/transfer-transaction")
@RequiredArgsConstructor
public class TransferTransactionController {
	private final TransactionService transactionService;

	@PostMapping("/{transferTransactionId}/accept")
	public void acceptTransfer(@PathVariable Long transferTransactionId,
		@RequestBody @Valid TransferAcceptReq transferAcceptReq) {
		transactionService.acceptTransfer(transferTransactionId, transferAcceptReq);
	}
}
