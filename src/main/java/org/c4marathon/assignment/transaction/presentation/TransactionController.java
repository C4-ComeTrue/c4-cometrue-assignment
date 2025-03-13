package org.c4marathon.assignment.transaction.presentation;

import org.c4marathon.assignment.global.annotation.Login;
import org.c4marathon.assignment.global.model.PageInfo;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.c4marathon.assignment.transaction.dto.TransactionGetRequest;
import org.c4marathon.assignment.transaction.dto.TransactionResponse;
import org.c4marathon.assignment.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {
	private final TransactionService transactionService;

	@GetMapping("/transaction")
	public ResponseEntity<PageInfo<TransactionResponse>> getTransaction(
		@Login SessionMemberInfo loginMember,
		@Valid @RequestBody TransactionGetRequest request
	) {
		return ResponseEntity.ok().body(transactionService.getTransactions(loginMember.accountNumber(), request));
	}
}
