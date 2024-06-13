package org.c4marathon.assignment.settlement.controller;

import org.c4marathon.assignment.common.annotation.Login;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.c4marathon.assignment.settlement.dto.request.DivideMoneyRequestDto;
import org.c4marathon.assignment.settlement.service.SettlementService;
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
@RequestMapping("/api/settle")
public class SettlementController {

	private final SettlementService settleService;

	/**
	 * 1/n 정산
	 */
	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/equally")
	public void divideMoneyEqually(@Login SessionMemberInfo memberInfo,
		@Valid @RequestBody DivideMoneyRequestDto requestDto) {
		settleService.divideEqually(memberInfo.mainAccountPk(), memberInfo.memberName(), requestDto);
	}

	/**
	 * 랜덤 정산
	 */
	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/random")
	public void divideMoneyRandom(@Login SessionMemberInfo memberInfo,
		@Valid @RequestBody DivideMoneyRequestDto requestDto) {
		settleService.divideRandom(memberInfo.mainAccountPk(), memberInfo.memberName(), requestDto);
	}

}
