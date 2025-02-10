package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.dto.request.PostSettlementReq;
import org.c4marathon.assignment.dto.response.SettlementInfoRes;
import org.c4marathon.assignment.service.SettlementService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/settlement")
@RequiredArgsConstructor
public class SettlementController {
	private final SettlementService settlementService;

	@PostMapping
	public SettlementInfoRes requestSettlement(@RequestBody @Valid PostSettlementReq settlementReq) {
		return settlementService.requestSettlement(settlementReq);
	}
}
