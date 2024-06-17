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
	 * 정산은 requestDto의 isRandom이 true인 경우 랜덤, false인 경우 1/n 정산을 한다.
	 *
	 */
	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/divide")
	public void divideMoney(@Login SessionMemberInfo memberInfo,
		@Valid @RequestBody DivideMoneyRequestDto requestDto) {
		settleService.divideMoney(memberInfo.mainAccountPk(), memberInfo.memberName(), requestDto);
	}

}
