package org.c4marathon.assignment.settlement.presentation;

import java.util.List;

import org.c4marathon.assignment.global.annotation.Login;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.c4marathon.assignment.settlement.dto.ReceivedSettlementResponse;
import org.c4marathon.assignment.settlement.dto.SettlementRequest;
import org.c4marathon.assignment.settlement.dto.SettlementResponse;
import org.c4marathon.assignment.settlement.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettlementController {
	private final SettlementService settlementService;

	@PostMapping("/settle")
	public ResponseEntity<Void> settle(
		@Login SessionMemberInfo loginMember,
		@RequestBody @Valid SettlementRequest request
	) {
		settlementService.createSettlement(loginMember.accountId(), request);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/settlements/requested")
	public ResponseEntity<List<SettlementResponse>> getRequestedSettlements(@Login SessionMemberInfo loginMember) {
		return ResponseEntity.ok().body(settlementService.getRequestedSettlements(loginMember.accountId()));
	}

	@GetMapping("/settlements/received")
	public ResponseEntity<List<ReceivedSettlementResponse>> getReceivedSettlements(@Login SessionMemberInfo loginMember) {
		return ResponseEntity.ok().body(settlementService.getReceivedSettlements(loginMember.accountId()));
	}
}
