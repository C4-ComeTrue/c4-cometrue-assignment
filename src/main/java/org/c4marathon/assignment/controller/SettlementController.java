package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.dto.SuccessNonDataResponse;
import org.c4marathon.assignment.common.exception.enums.SuccessCode;
import org.c4marathon.assignment.dto.request.SettleMemberRequestDto;
import org.c4marathon.assignment.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.service.SettlementService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/settle")
@RequiredArgsConstructor
public class SettlementController {
	private final SettlementService settleService;

	@PostMapping("/request")
	public SuccessNonDataResponse requestSettlement(@RequestBody @Valid SettlementRequestDto requestDto, @RequestBody @Valid
		SettleMemberRequestDto memberRequestDto) {
		settleService.divideMoney(requestDto, memberRequestDto);
		return SuccessNonDataResponse.success(SuccessCode.REQUEST_SETTLEMENT_SUCCESS);
	}

	@PostMapping("/remittance")
	public SuccessNonDataResponse remittanceSettlement(@RequestParam(value = "settlementMemberId") long settlementMemberId) {
		settleService.remittanceMoney(settlementMemberId);
		return SuccessNonDataResponse.success(SuccessCode.REMITTANCE_SETTLEMENT_SUCCESS);
	}

}
